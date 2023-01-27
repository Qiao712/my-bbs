# post 文档的约束
```json
{
    "mappings":{
        "properties":{
         	"id":{
                "type":"long",
                "index": false
            },

            "title":{
                "type":"text",
                "analyzer": "ik_smart"
            },

            "content":{
                "type":"text",
                "analyzer":"ik_smart"
            },

            "authorId":{
                "type":"long"
            },

            "forumId":{
                "type":"long"
            },

            "createTime":{
                "type":"date"
            },

            "updateTime":{
                "type":"date"
            }
        }
    }
}
```