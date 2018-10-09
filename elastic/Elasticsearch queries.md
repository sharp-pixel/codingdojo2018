GET /twitter/_search
{
  "query": {
    "match_all": {}
  }
}

GET _search
{
  "query": {
    "match_all": {}
  }
}

POST /twitter/_search
{
  "query": {
    "term" : { "User.Name.keyword": "iBrock" } 
  }
}

POST /twitter/_search
{
  "query": {
    "bool": {
      "filter": {
        "term": {
          "User.Name.keyword": "iBrock"
        }
      }
    }
  }
}

POST /twitter/_search
{
  "query": {
    "term" : { "User.Name.keyword": "iBrock" } 
  }
}


GET /twitter/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "User.Name": "iBrock"
          }
        }
      ]
    }
  }
}

GET /twitter/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "User.Location": "United States"
          }
        }
      ]
    }
  }
}

POST /twitter/_search
{
  "query": {
    "bool": {
      "filter": {
        "term": {
          "User.Location.keyword": "United States"
        }
      }
    }
  }
}

GET /twitter/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "exists": {
            "field": "GeoLocation"
          }
        }
      ]
    }
  }
}

GET /twitter/_mapping

GET /twitter/_search
{
    "query": {
        "regexp":{
            "User.Name.keyword": "A.*"
        }
    }
}

GET /_cat/shards?v