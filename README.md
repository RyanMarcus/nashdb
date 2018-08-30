# NashDB

This is the public implementation of NashDB, an economics-driven approach to cloud database fragmentation, replication, and provisioning. It is free software available under the AGPL-3.0 license. It implements many of the algorithms from the paper:

[Ryan Marcus, Olga Papaemmanouil, Sofiya Semenova, and Solomon Garber. NashDB: An Economic Approach to Fragmentation, Replication and Provisioning for Elastic Databases 37th ACM Special Interest Group in Data Management 2018 (SIGMOD '18)](https://api.zotero.org/users/3604318/publications/items/35KTECTC/file/view)

You can watch my presentation at SIGMOD [here](https://www.youtube.com/watch?v=EDDwmx_L-p0).

NashDB is available under the AGPL-3.0 license.

## Usage

The primary entry point for the public-facing API is [`NashDB.java`](https://github.com/RyanMarcus/nashdb/blob/master/src/main/java/edu/brandeis/nashdb/NashDB.java). [Check out the examples](https://github.com/RyanMarcus/nashdb/tree/master/src/main/java/edu/brandeis/nashdb/examples) and the JavaDoc comments in the `NashDB` class.

The `NashDB` class has a `toJSON` method that will automatically produce fragments, VM assignments, and a tuple value graph. The output looks like this:

```json
{
  "graphData": [
    {
      "start": 0,
      "end": 1,
      "value": 15
    },
    {
      "start": 1,
      "end": 2,
      "value": 23
    },
    {
      "start": 2,
      "end": 3,
      "value": 29
    },
    {
      "start": 3,
      "end": 4,
      "value": 36
    },
    {
      "start": 4,
      "end": 5,
      "value": 44
    },
    {
      "start": 5,
      "end": 6,
      "value": 50
    },
    {
      "start": 6,
      "end": 7,
      "value": 54
    },
    {
      "start": 7,
      "end": 8,
      "value": 66
    },
    {
      "start": 8,
      "end": 9,
      "value": 71
    },
    {
      "start": 9,
      "end": 10,
      "value": 79
    },
    {
      "start": 10,
      "end": 11,
      "value": 81
    },
    {
      "start": 11,
      "end": 12,
      "value": 94
    },
    {
      "start": 12,
      "end": 13,
      "value": 100
    },
    {
      "start": 13,
      "end": 14,
      "value": 107
    },
    {
      "start": 14,
      "end": 15,
      "value": 118
    },
    {
      "start": 15,
      "end": 16,
      "value": 124
    },
    {
      "start": 16,
      "end": 17,
      "value": 127
    },
    {
      "start": 17,
      "end": 18,
      "value": 136
    },
    {
      "start": 18,
      "end": 19,
      "value": 143
    },
    {
      "start": 19,
      "end": 20,
      "value": 151
    },
    {
      "start": 20,
      "end": 21,
      "value": 159
    },
    {
      "start": 21,
      "end": 22,
      "value": 160
    },
    {
      "start": 22,
      "end": 23,
      "value": 169
    },
    {
      "start": 23,
      "end": 24,
      "value": 175
    },
    {
      "start": 24,
      "end": 25,
      "value": 181
    },
    {
      "start": 25,
      "end": 26,
      "value": 189
    },
    {
      "start": 26,
      "end": 27,
      "value": 197
    },
    {
      "start": 27,
      "end": 28,
      "value": 203
    },
    {
      "start": 28,
      "end": 29,
      "value": 207
    },
    {
      "start": 29,
      "end": 30,
      "value": 202
    },
    {
      "start": 30,
      "end": 31,
      "value": 213
    },
    {
      "start": 31,
      "end": 32,
      "value": 217
    },
    {
      "start": 32,
      "end": 33,
      "value": 215
    },
    {
      "start": 33,
      "end": 34,
      "value": 219
    },
    {
      "start": 34,
      "end": 35,
      "value": 222
    },
    {
      "start": 35,
      "end": 36,
      "value": 220
    },
    {
      "start": 36,
      "end": 37,
      "value": 224
    },
    {
      "start": 37,
      "end": 38,
      "value": 222
    },
    {
      "start": 38,
      "end": 39,
      "value": 227
    },
    {
      "start": 39,
      "end": 40,
      "value": 224
    },
    {
      "start": 40,
      "end": 41,
      "value": 226
    },
    {
      "start": 41,
      "end": 42,
      "value": 229
    },
    {
      "start": 42,
      "end": 43,
      "value": 228
    },
    {
      "start": 43,
      "end": 44,
      "value": 228
    },
    {
      "start": 44,
      "end": 45,
      "value": 231
    },
    {
      "start": 45,
      "end": 46,
      "value": 235
    },
    {
      "start": 46,
      "end": 47,
      "value": 235
    },
    {
      "start": 47,
      "end": 48,
      "value": 240
    },
    {
      "start": 48,
      "end": 49,
      "value": 241
    },
    {
      "start": 49,
      "end": 50,
      "value": 247
    },
    {
      "start": 50,
      "end": 51,
      "value": 244
    },
    {
      "start": 51,
      "end": 52,
      "value": 247
    },
    {
      "start": 52,
      "end": 53,
      "value": 247
    },
    {
      "start": 53,
      "end": 54,
      "value": 248
    },
    {
      "start": 54,
      "end": 55,
      "value": 246
    },
    {
      "start": 55,
      "end": 56,
      "value": 240
    },
    {
      "start": 56,
      "end": 57,
      "value": 238
    },
    {
      "start": 57,
      "end": 58,
      "value": 239
    },
    {
      "start": 58,
      "end": 59,
      "value": 239
    },
    {
      "start": 59,
      "end": 60,
      "value": 235
    },
    {
      "start": 60,
      "end": 61,
      "value": 236
    },
    {
      "start": 61,
      "end": 62,
      "value": 235
    },
    {
      "start": 62,
      "end": 63,
      "value": 234
    },
    {
      "start": 63,
      "end": 64,
      "value": 233
    },
    {
      "start": 64,
      "end": 65,
      "value": 228
    },
    {
      "start": 65,
      "end": 66,
      "value": 224
    },
    {
      "start": 66,
      "end": 67,
      "value": 223
    },
    {
      "start": 67,
      "end": 68,
      "value": 217
    },
    {
      "start": 68,
      "end": 69,
      "value": 214
    },
    {
      "start": 69,
      "end": 70,
      "value": 213
    },
    {
      "start": 70,
      "end": 71,
      "value": 211
    },
    {
      "start": 71,
      "end": 72,
      "value": 206
    },
    {
      "start": 72,
      "end": 73,
      "value": 200
    },
    {
      "start": 73,
      "end": 74,
      "value": 200
    },
    {
      "start": 74,
      "end": 75,
      "value": 197
    },
    {
      "start": 75,
      "end": 76,
      "value": 192
    },
    {
      "start": 76,
      "end": 77,
      "value": 189
    },
    {
      "start": 77,
      "end": 78,
      "value": 190
    },
    {
      "start": 78,
      "end": 79,
      "value": 187
    },
    {
      "start": 79,
      "end": 80,
      "value": 186
    },
    {
      "start": 80,
      "end": 81,
      "value": 179
    },
    {
      "start": 81,
      "end": 82,
      "value": 174
    },
    {
      "start": 82,
      "end": 83,
      "value": 162
    },
    {
      "start": 83,
      "end": 84,
      "value": 147
    },
    {
      "start": 84,
      "end": 85,
      "value": 142
    },
    {
      "start": 85,
      "end": 86,
      "value": 135
    },
    {
      "start": 86,
      "end": 87,
      "value": 129
    },
    {
      "start": 87,
      "end": 88,
      "value": 117
    },
    {
      "start": 88,
      "end": 89,
      "value": 111
    },
    {
      "start": 89,
      "end": 90,
      "value": 103
    },
    {
      "start": 90,
      "end": 91,
      "value": 94
    },
    {
      "start": 91,
      "end": 92,
      "value": 88
    },
    {
      "start": 92,
      "end": 93,
      "value": 73
    },
    {
      "start": 93,
      "end": 94,
      "value": 68
    },
    {
      "start": 94,
      "end": 95,
      "value": 55
    },
    {
      "start": 95,
      "end": 96,
      "value": 39
    },
    {
      "start": 96,
      "end": 97,
      "value": 32
    },
    {
      "start": 97,
      "end": 98,
      "value": 18
    },
    {
      "start": 98,
      "end": 99,
      "value": 7
    }
  ],
  "fragments": [
    {
      "start": 0,
      "end": 12
    },
    {
      "start": 12,
      "end": 24
    },
    {
      "start": 24,
      "end": 82
    },
    {
      "start": 82,
      "end": 92
    },
    {
      "start": 92,
      "end": 100
    }
  ],
  "vms": [
    {
      "fragments": [
        4,
        1,
        3,
        0,
        2
      ]
    },
    {
      "fragments": [
        0,
        4,
        3,
        1,
        2
      ]
    },
    {
      "fragments": [
        1,
        0,
        2,
        3,
        4
      ]
    },
    {
      "fragments": [
        1,
        2,
        0,
        3,
        4
      ]
    },
    {
      "fragments": [
        2,
        0,
        3,
        1,
        4
      ]
    },
    {
      "fragments": [
        4,
        0,
        2,
        1,
        3
      ]
    }
  ]
}
```
