package github.qiao712.bbs.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import github.qiao712.bbs.config.SystemConfig;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.entity.Post;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.service.SearchService;
import github.qiao712.bbs.util.HtmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
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
    private PostMapper postMapper;

    private RestHighLevelClient restClient;

    //???????????????
    private static final String POST_INDEX = "post";

    //?????????????????????
    private static final Set<String> sortableFields = new HashSet<>();
    static {
        sortableFields.add("createTime");
        sortableFields.add("updateTime");
    }

    /**
     * ?????????ElasticSearch RestClient
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        SystemConfig.ElasticSearch elasticSearchConfig = systemConfig.getElasticSearch();
        String[] hosts = elasticSearchConfig.getHosts();
        String username = elasticSearchConfig.getUsername();
        String password = elasticSearchConfig.getPassword();

        if(hosts == null || hosts.length == 0){
            log.error("?????????ElasticSearch??????");
            return;
        }

        //????????????
        HttpHost[] httpHosts = new HttpHost[hosts.length];
        for (int i = 0; i<hosts.length; i++) {
            httpHosts[i] = HttpHost.create(hosts[i]);
            log.info("ElasticSearch: {}", hosts[i]);
        }
        RestClientBuilder clientBuilder = RestClient.builder(httpHosts);

        //??????Basic??????
        if(username != null){
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            clientBuilder.setHttpClientConfigCallback(httpAsyncClientBuilder -> httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }

        restClient = new RestHighLevelClient(clientBuilder);
    }

    /**
     * ??????RestClient
     */
    @Override
    public void destroy() throws Exception {
        restClient.close();
    }

    @Override
    public void savePost(Post post) {
        if(post == null || post.getId() == null) throw new ServiceException("Post/Post.id ????????????");

        //??????html??????
        if(post.getContent() != null) post.setContent(HtmlUtil.getText(post.getContent()));
        post.setLikeCount(null);

        String postJson = toJSONString(post);
        IndexRequest request = new IndexRequest(POST_INDEX).id(post.getId().toString());
        request.source(postJson, XContentType.JSON);
        try {
            restClient.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Post??????????????????", e);
        }
    }

    @Override
    public void removePost(Long postId) {
        DeleteRequest request = new DeleteRequest(POST_INDEX, postId.toString());
        try {
            restClient.delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Post??????????????????", e);
        }
    }

    @Override
    public void updatePost(Post post) {
        if(post == null || post.getId() == null) throw new ServiceException("Post/Post.id ????????????");

        UpdateRequest request = new UpdateRequest(POST_INDEX, post.getId().toString());

        if(post.getContent() != null) post.setContent(HtmlUtil.getText(post.getContent()));
        String postJson = JSON.toJSONString(post);
        request.doc(postJson, XContentType.JSON);

        try {
            restClient.update(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Post??????????????????", e);
        }
    }

    @Override
    public Post getPostDoc(Long postId) {
        GetRequest request = new GetRequest(POST_INDEX, postId.toString());
        GetResponse response = null;
        try {
            response = restClient.get(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("Post??????????????????", e);
        }

        if(response != null && response.getSourceAsString() != null){
            return JSON.parseObject(response.getSourceAsString(), Post.class);
        }else{
            log.error("Post??????????????????");
            return null;
        }
    }

    @Override
    public IPage<Post> searchPosts(PageQuery pageQuery, String text, Long authorId, Long forumId) {
        SearchRequest request = new SearchRequest(POST_INDEX);

        //??????????????????(DSL)
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.multiMatchQuery(text, "title", "content"));  //???????????????????????????
        if(authorId != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("authorId", authorId));           //????????????
        }
        if(forumId != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("forumId", forumId));             //????????????
        }

        //????????????
        request.source().size(pageQuery.getPageSize());
        int from = (pageQuery.getPageNo() - 1) * pageQuery.getPageSize();
        from = Math.max(from, 0);
        request.source().from(from);

        //????????????
        if(pageQuery.getOrderBy() != null && sortableFields.contains(pageQuery.getOrderBy())){
            SortOrder sortOrder = SortOrder.DESC;   //????????????
            if(pageQuery.getOrder() != null && Objects.equals(pageQuery.getOrder().toUpperCase(), "ASC")){
                sortOrder = SortOrder.ASC;
            }
            request.source().sort(pageQuery.getOrderBy(), sortOrder);
        }

        //??????????????????
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title").field("content");
        highlightBuilder.preTags("<em>").postTags("</em>");
        request.source().highlighter(highlightBuilder);

        //??????
        request.source().query(boolQueryBuilder);
        SearchResponse response = null;
        try {
             response = restClient.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("????????????", e);
        }
        if(response == null) throw new RuntimeException("????????????");

        //????????????
        Page<Post> postPage = new Page<>();
        postPage.setCurrent(pageQuery.getPageNo());
        postPage.setSize(pageQuery.getPageSize());
        if(response.getHits() == null){
            postPage.setTotal(0);
            postPage.setRecords(Collections.emptyList());
            return postPage;
        }

        List<Post> posts = new ArrayList<>(pageQuery.getPageSize());
        for (SearchHit hit : response.getHits()) {
            Post post = JSON.parseObject(hit.getSourceAsString(), Post.class);

            //?????????????????????
            HighlightField highlightTitle = hit.getHighlightFields().get("title");
            HighlightField highlightContent = hit.getHighlightFields().get("content");
            if(highlightTitle != null){
                post.setTitle(highlightTitle.getFragments()[0].toString());
            }
            if(highlightContent != null){
                post.setContent(highlightContent.getFragments()[0].toString());
            }

            posts.add(post);
        }
        postPage.setTotal(response.getHits().getTotalHits().value);
        postPage.setRecords(posts);

        return postPage;
    }

    @Override
    public void syncAllPosts() {
        Page<Post> postPage = new Page<>(1, 100);

        do{
            //????????????
            BulkRequest bulkRequest = new BulkRequest();
            postMapper.selectPage(postPage, new QueryWrapper<>());

            for (Post post : postPage.getRecords()) {
                IndexRequest indexRequest = new IndexRequest(POST_INDEX).id(post.getId().toString());

                post.setContent(HtmlUtil.getText(post.getContent()));
                post.setLikeCount(null);
                String postJson = toJSONString(post);
                indexRequest.source(postJson, XContentType.JSON);

                bulkRequest.add(indexRequest);
            }
            try {
                restClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                log.error("??????????????????", e);
            }

            //?????????
            postPage.setCurrent(postPage.getCurrent() + 1);
        }while (postPage.getCurrent() <= postPage.getPages());

        log.info("????????????, ?????????{}???Post", postPage.getTotal());
    }
}
