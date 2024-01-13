package qiao.qasys.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import qiao.qasys.common.PageQuery;
import qiao.qasys.config.SystemConfig;
import qiao.qasys.exception.ServiceException;
import qiao.qasys.service.SearchService;
import qiao.qasys.entity.Question;
import qiao.qasys.common.ResultCode;
import qiao.qasys.mapper.QuestionMapper;
import qiao.qasys.util.HtmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static com.alibaba.fastjson.JSON.toJSONString;

@Service
@Slf4j
public class SearchServiceImpl implements SearchService, InitializingBean, DisposableBean {
    @Autowired
    private SystemConfig systemConfig;
    @Autowired
    private QuestionMapper questionMapper;

    private RestHighLevelClient restClient;

    //索引库名称
    private static final String POST_INDEX = "question";

    //可以排序的字段
    private static final Set<String> sortableFields = new HashSet<>();
    static {
        sortableFields.add("createTime");
        sortableFields.add("updateTime");
    }

    /**
     * 初始化ElasticSearch RestClient
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        SystemConfig.ElasticSearch elasticSearchConfig = systemConfig.getElasticSearch();
        String[] hosts = elasticSearchConfig.getHosts();
        String username = elasticSearchConfig.getUsername();
        String password = elasticSearchConfig.getPassword();

        if(hosts == null || hosts.length == 0){
            log.error("未配置ElasticSearch地址");
            return;
        }

        //设置地址
        HttpHost[] httpHosts = new HttpHost[hosts.length];
        for (int i = 0; i<hosts.length; i++) {
            httpHosts[i] = HttpHost.create(hosts[i]);
            log.info("ElasticSearch: {}", hosts[i]);
        }
        RestClientBuilder clientBuilder = RestClient.builder(httpHosts);

        //设置Basic认证
        if(username != null){
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            clientBuilder.setHttpClientConfigCallback(httpAsyncClientBuilder -> httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }

        restClient = new RestHighLevelClient(clientBuilder);
    }

    /**
     * 关闭RestClient
     */
    @Override
    public void destroy() throws Exception {
        restClient.close();
    }

    @Override
    public void saveQuestion(Question question) throws IOException {
        if(question == null || question.getId() == null) throw new ServiceException(ResultCode.INVALID_PARAM, "Post/Post.id 不可为空");

        //去除html样式
        if(question.getContent() != null) question.setContent(HtmlUtil.getText(question.getContent()));
        question.setLikeCount(null);

        String postJson = toJSONString(question);
        IndexRequest request = new IndexRequest(POST_INDEX).id(question.getId().toString());
        request.source(postJson, XContentType.JSON);
        IndexResponse response = restClient.index(request, RequestOptions.DEFAULT);   //抛出IO错误

        //response.getShardInfo().getSuccessful() 成功写入的分片数量
        if(response.getShardInfo() == null || response.getShardInfo().getSuccessful() == 0){
            throw new ServiceException(ResultCode.FAILURE, "Post文档储存失败");
        }
    }

    @Override
    public void removeQuestion(Long questionId) throws IOException {
        DeleteRequest request = new DeleteRequest(POST_INDEX, questionId.toString());
        DeleteResponse response = restClient.delete(request, RequestOptions.DEFAULT);

        if(response.getShardInfo() == null || response.getShardInfo().getSuccessful() == 0){
            throw new ServiceException(ResultCode.FAILURE, "Post文档删除失败");
        }
    }

    @Override
    public void updateQuestion(Question question) throws IOException {
        if(question == null || question.getId() == null) throw new ServiceException(ResultCode.INVALID_PARAM,"Post/Post.id 不可为空");

        UpdateRequest request = new UpdateRequest(POST_INDEX, question.getId().toString());

        if(question.getContent() != null) question.setContent(HtmlUtil.getText(question.getContent()));
        String postJson = JSON.toJSONString(question);
        request.doc(postJson, XContentType.JSON);

        UpdateResponse response = restClient.update(request, RequestOptions.DEFAULT);
        if(response.getShardInfo() == null || response.getShardInfo().getSuccessful() == 0){
            throw  new ServiceException(ResultCode.FAILURE, "Post文档更新失败");
        }
    }

    @Override
    public Question getQuestionDoc(Long questionId) {
        GetRequest request = new GetRequest(POST_INDEX, questionId.toString());
        GetResponse response = null;
        try {
            response = restClient.get(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Post文档查询失败", e);
        }

        if(response != null && response.getSourceAsString() != null){
            return JSON.parseObject(response.getSourceAsString(), Question.class);
        }else{
            log.error("Post文档查询失败");
            return null;
        }
    }

    @Override
    public IPage<Question> searchQuestions(PageQuery pageQuery, String text, Long authorId, Long categoryId) {
        SearchRequest request = new SearchRequest(POST_INDEX);

        //定义查询语句(DSL)
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.multiMatchQuery(text, "title", "content"));  //在标题和内容中检索
        if(authorId != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("authorId", authorId));           //指定作者
        }
        if(categoryId != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("categoryId", categoryId));             //指定板块
        }

        //设置分页
        request.source().size(pageQuery.getPageSize());
        int from = (pageQuery.getPageNo() - 1) * pageQuery.getPageSize();
        from = Math.max(from, 0);
        request.source().from(from);

        //设置排序
        if(pageQuery.getOrderBy() != null && sortableFields.contains(pageQuery.getOrderBy())){
            SortOrder sortOrder = SortOrder.DESC;   //默认降序
            if(pageQuery.getOrder() != null && Objects.equals(pageQuery.getOrder().toUpperCase(), "ASC")){
                sortOrder = SortOrder.ASC;
            }
            request.source().sort(pageQuery.getOrderBy(), sortOrder);
        }

        //设置高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title").field("content");
        highlightBuilder.preTags("<em>").postTags("</em>");
        request.source().highlighter(highlightBuilder);

        //搜索
        request.source().query(boolQueryBuilder);
        SearchResponse response = null;
        try {
             response = restClient.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("搜索失败", e);
        }
        if(response == null) throw new RuntimeException("搜索失败");

        //处理结果
        Page<Question> postPage = new Page<>();
        postPage.setCurrent(pageQuery.getPageNo());
        postPage.setSize(pageQuery.getPageSize());
        if(response.getHits() == null){
            postPage.setTotal(0);
            postPage.setRecords(Collections.emptyList());
            return postPage;
        }

        List<Question> questions = new ArrayList<>(pageQuery.getPageSize());
        for (SearchHit hit : response.getHits()) {
            Question question = JSON.parseObject(hit.getSourceAsString(), Question.class);

            //设置高亮的结果
            HighlightField highlightTitle = hit.getHighlightFields().get("title");
            HighlightField highlightContent = hit.getHighlightFields().get("content");
            if(highlightTitle != null){
                question.setTitle(highlightTitle.getFragments()[0].toString());
            }
            if(highlightContent != null){
                question.setContent(highlightContent.getFragments()[0].toString());
            }

            questions.add(question);
        }
        postPage.setTotal(response.getHits().getTotalHits().value);
        postPage.setRecords(questions);

        return postPage;
    }

    @Override
    public void syncAllQuestion() {
        Page<Question> postPage = new Page<>(1, 100);

        do{
            //添加一批
            BulkRequest bulkRequest = new BulkRequest();
            questionMapper.selectPage(postPage, new QueryWrapper<>());

            for (Question question : postPage.getRecords()) {
                IndexRequest indexRequest = new IndexRequest(POST_INDEX).id(question.getId().toString());

                question.setContent(HtmlUtil.getText(question.getContent()));
                question.setLikeCount(null);
                String postJson = toJSONString(question);
                indexRequest.source(postJson, XContentType.JSON);

                bulkRequest.add(indexRequest);
            }
            try {
                restClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                log.error("批量添加失败", e);
            }

            //下一页
            postPage.setCurrent(postPage.getCurrent() + 1);
        }while (postPage.getCurrent() <= postPage.getPages());

        log.info("同步完成, 已保存{}个Post", postPage.getTotal());
    }
}
