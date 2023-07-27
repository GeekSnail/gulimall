package com.example.gulimall.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Data
//@Document(indexName = "users")
class User {
//    @Id
    String id;
//    @Field(type=FieldType.Text)
    String name;
//    @Field(type=FieldType.Integer)
    Integer age;
//    @Field(type=FieldType.Keyword)
    String gender;
}

@Data
class Account {
    private int account_number;
    private int balance;
    private String firstname;
    private String lastname;
    private int age;
    private String gender;
    private String address;
    private String employer;
    private String email;
    private String city;
    private String state;
}

@SpringBootTest
class GulimallSearchApplicationTests {
    @Autowired
    RestClient restClient;

    @Test
    void contextLoads() throws IOException {
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());
        ElasticsearchClient client = new ElasticsearchClient(transport);
        User user = new User("1","Jack", 20, "男");
        IndexRequest iReq = IndexRequest.of(i->i.index("users").id("1").document(user));
        IndexResponse iRes = client.index(i->i.index("users").id(user.getId()).document(user));
        System.out.println(iRes);
    }

    @Autowired
    ElasticsearchClient elasticsearchClient;
    @Test
    void testCRUD() throws IOException {
        //index
        User user = new User("4","Linda", 18, "男");
        IndexResponse iRes = elasticsearchClient.index(i->i.index("users").id(user.getId()).document(user));
        System.out.println(iRes);
        //get
        GetResponse<User> gRes = elasticsearchClient.get(g -> g.index("users").id(user.getId()), User.class);
        if (gRes.found()) {
            User u = gRes.source();
            System.out.println(u);
        } else {
            System.out.println(user.getId() + " does not exists.");
        }
        //searchAll
        SearchRequest searchRequest = SearchRequest.of(s -> s.index("users"));
        SearchResponse<User> sRes = elasticsearchClient.search(searchRequest, User.class);
        List<Hit<User>> hits = sRes.hits().hits();
        List<User> collect = hits.stream().map(hit -> (User) hit.source()).collect(Collectors.toList());
        System.out.println(collect);
        //delete
        DeleteRequest deleteRequest = DeleteRequest.of(d -> d.index("users").id(user.getId()));
        DeleteResponse dRes = elasticsearchClient.delete(deleteRequest);
        if (Objects.nonNull(dRes.result()) && !dRes.result().name().equals("NotFound")) {
            System.out.println(user.getId() + " has been deleted.");
        } else {
            System.out.println(user.getId() + " does not exists.");
        }
    }

    @Test
    void testESAggregation() throws IOException {
//        Query query = MatchQuery.of(m -> m.field("address").query("mill"))._toQuery();
        SearchRequest searchRequest = SearchRequest.of(s -> s.index("newbank")
                .query(q -> q.match(m->m.field("address").query("mill")))
                .aggregations("ageAgg", a -> a.terms(t -> t.field("age").size(10)))
                .aggregations("ageAvg", ag -> ag.avg(a -> a.field("age")))
                .aggregations("balanceAvg", ag -> ag.avg(a -> a.field("balance"))));
        SearchResponse<Account> sRes = elasticsearchClient.search(searchRequest, Account.class);
        TotalHits total = sRes.hits().total();
        System.out.println(total);
        List<Hit<Account>> hits = sRes.hits().hits();
        List<Account> collect = hits.stream().map(Hit::source).collect(Collectors.toList());
        System.out.println(collect);
        sRes.aggregations().forEach((k,v)-> {
            if (v.isLterms()) {
                List<LongTermsBucket> buckets = v.lterms().buckets().array();
                buckets.forEach(System.out::println);
            } else {
                System.out.println(v);
            }
        });
    }

    @Test
    void testBulk() throws IOException {
        ArrayList<User> users = new ArrayList<>();
        users.add(new User("5", "Linda", 18, "女"));
        users.add(new User("6", "Lisa", 18, "女"));
//        List<BulkOperation> operations = new ArrayList<>();
//        for (User user: users) {
//            operations.add(BulkOperation.of(op -> op.index(i -> i.id("users").document(user))));
//        }
        List<BulkOperation> ios = users.stream().map(u ->
                BulkOperation.of(op -> op.index(i -> i.index("users").id(u.getId()).document(u))))
                .collect(Collectors.toList());
        BulkResponse ires = elasticsearchClient.bulk(b -> b.operations(ios));
        System.out.println(ires);
        List<BulkOperation> dos = users.stream().map(u ->
                BulkOperation.of(op -> op.delete(i -> i.index("users").id(u.getId()))))
                .collect(Collectors.toList());
        BulkResponse dres = elasticsearchClient.bulk(b -> b.operations(dos));
        System.out.println(dres);
    }
    @Test
    void testExists() throws IOException {
        BooleanResponse res = elasticsearchClient.indices().exists(e -> e.index("users"));
        System.out.println(res.value()); //true
        res = elasticsearchClient.indices().exists(e -> e.index("users", "test"));
        System.out.println(res.value()); //false

    }

    @Test
    void testInputStream() throws FileNotFoundException {
//        FileInputStream inputStream = new FileInputStream("classpath:mapping/product.json");
        InputStream inputStream = getClass().getResourceAsStream("/index/product.json");
        System.out.println(inputStream);
    }
}
