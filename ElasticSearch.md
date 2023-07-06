### 索引库mapping
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

            "categoryId":{
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