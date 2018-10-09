GET /parcels/_search
{
  "query": {
    "match_all": {}
  }
}

PUT _template/parcel_1
{
  "index_patterns": ["parcel*"],
  "settings": {"number_of_shards": 5},
  "mappings": {
    "parcel": {
      "_source": {"enabled": true},
      "properties": {
        "id": {"type": "keyword"},
        "time": {"type": "date"},
        "status": {"type": "keyword"}
      },
      "dynamic": false
    }
  },
  "aliases": {
    "parcels": {}
  }
}

PUT /parcels-000001
{
  "aliases": {
    "parcels_write": {}
  }
}

POST /parcels_write/parcel
{
  "id": "1",
  "time": "2018-10-01T12:00:00Z",
  "status": "READY"
}

POST /parcels_write/parcel
{
  "id": "2",
  "time": "2018-10-01T14:00:00Z",
  "status": "READY"
}

POST /parcels_write/parcel
{
  "id": "1",
  "time": "2018-10-02T09:00:00Z",
  "status": "IN_TRANSIT"
}

POST /parcels_write/parcel
{
  "id": "2",
  "time": "2018-10-02T14:00:00Z",
  "status": "ON_HOLD"
}

POST /parcels_write/_rollover
{
  "conditions" : {
    "max_docs": 2
  }
}

GET _cat/indices

POST /parcels_write/parcel
{
  "id": "1",
  "time": "2018-10-03T09:00:00Z",
  "status": "DELIVERED"
}

POST /parcels_write/parcel
{
  "id": "3",
  "time": "2018-10-01T14:00:00Z",
  "status": "READY"
}

POST /parcels_write/parcel
{
  "id": "4",
  "time": "2018-10-03T12:00:00Z",
  "status": "DELIVERED"
}

POST /parcels_write/parcel
{
  "id": "5",
  "time": "2018-10-03T12:00:00Z",
  "status": "READY"
}

GET /parcels/_search
{
  "aggs": {
    "parcel-id": {
      "terms": {
        "field": "id",
        "size": 20
      },
      "aggs": {
        "latest-event": {
          "top_hits": {
            "sort": [
              {
                "time": {
                  "order": "desc"
                }
              }
            ],
            "_source": {
              "includes": "*"
            },
            "size": 1
          }
        }
      }
    }
  },
  "size": 0
}

GET /parcels/_search
{
  "aggs": {
    "parcel-id": {
      "terms": {
        "field": "id",
        "size": 20
      },
      "aggs": {
        "latest-event": {
          "top_hits": {
            "sort": [
              {
                "time": {
                  "order": "desc"
                }
              }
            ],
            "_source": {
              "includes": "*"
            },
            "size": 1
          },
          "aggs": {
            "ready": {
              "filter": {
                "term": {
                  "status": "READY"
                }
              },
              "_source": {
                "includes": "*"
              }
            }
          }
        }
      }
    }
  },
  "size": 0
}

GET /parcels/_search
{
  "aggs": {
    "parcel-id": {
      "terms": {
        "field": "id",
        "size": 20
      },
      "aggs": {
        "order-time": {
          "terms": {
            "field": "time",
            "order": {
              "_key": "desc"
            },
            "size": 1
          },
          "aggs": {
            "ready": {
              "filter": {
                "term": {
                  "status": "DELIVERED"
                }
              },
              "aggs": {
                "latest-event": {
                  "top_hits": {
                    "_source": {
                      "includes": "*"
                    },
                    "size": 1
                  }
                }
              }
            },
            "hit_bucket_filter": {
              "bucket_selector": {
                "buckets_path": {
                  "hit_count": "ready._count"
                },
                "script": "params.hit_count > 0"
              }
            }
          }
        },
        "time_bucket_filter": {
          "bucket_selector": {
            "buckets_path": {
              "hit_count": "order-time._bucket_count"
            },
            "script": "params.hit_count > 0"
          }
        }
      }
    }
  },
  "size": 0
}
