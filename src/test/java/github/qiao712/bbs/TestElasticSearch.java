package github.qiao712.bbs;

import github.qiao712.bbs.domain.entity.Post;
import github.qiao712.bbs.service.SearchService;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class TestElasticSearch {
    private RestHighLevelClient restClient;

    @BeforeEach
    public void init(){
        RestClientBuilder clientBuilder = RestClient.builder(
                HttpHost.create("121.36.85.97:9200")
        );

        //设置Basic登录
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "haihaihai"));
        clientBuilder.setHttpClientConfigCallback(httpAsyncClientBuilder -> httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

        restClient = new RestHighLevelClient(clientBuilder);
    }

    @Test
    public void ping() throws IOException {
        System.out.println(restClient.ping(RequestOptions.DEFAULT));
        restClient.close();
    }

    @Autowired
    private SearchService searchService;

    @Test
    public void testSyncAllPost(){
        searchService.syncAllPosts();
    }

    @Test
    public void testDeletePostDoc(){
        searchService.removePost(1L);
    }

    @Test
    public void testUpdatePostDoc(){
        Post post = new Post();
        post.setId(1L);
//        post.setContent("<p>Hello, wowowow<p/>");
//        post.setForumId(123L);
//        post.setAuthorId(222L);
        post.setTitle("He2222222llo nonono");
//        post.setUpdateTime(LocalDateTime.now());
        searchService.updatePost(post);
    }

    @Test
    public void testSearch(){
        SearchRequest request = new SearchRequest("post");

        //定义查询语句(DSL)
        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        QueryBuilders.rangeQuery("createTime");


        boolQueryBuilder.must(matchAllQueryBuilder);


        request.source().query(boolQueryBuilder);

        try {
            SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);
            for (SearchHit hit : response.getHits()) {
                System.out.println(hit.getSourceAsString());
            }
        } catch (IOException e) {

        }
    }
}
