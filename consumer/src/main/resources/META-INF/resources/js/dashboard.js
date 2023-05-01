"use strict"

var connected = false;
var socket;
var myChart1 = echarts.init(document.getElementById('main1'));
var myChart2 = echarts.init(document.getElementById('main2'));
let sData1 = [];
let sData2 = [];

// websocket connect to server
function connect() {
  if (!connected) {
    var clientId = generateClientId(6);
    socket = new WebSocket("ws://" + location.host + "/temps/" + clientId);

    socket.onopen = function () {
      connected = true;
      console.log("Connected to the web socket with clientId [" + clientId + "]");
      $("#connect").attr("disabled", true);
      $("#connect").text("Connected");
    };

    socket.onmessage = function (m) {
      //console.log("Got message: " + m.data);
      $("#message").text(m.data);
      let dobj = JSON.parse(m.data);
      if (dobj.stationId == 1) {
        $("#samplecount1").text(dobj.stationName + " Sample Count");
        now = new Date(dobj.timestamp);
        let d = {
          name: dobj.stationName,
          value: [
            dobj.timestamp,
            dobj.count
          ]
        };
        if (sData1.length > 5) {
          sData1.shift();
        }
        sData1.push(d);
        console.log(">> " + JSON.stringify(sData1));

        myChart1.setOption({
          series: [
            {
              data: sData1
            }
          ]
        });
      }
      if (dobj.stationId == 2) {
        $("#samplecount2").text(dobj.stationName + " Sample Count");
        now = new Date(dobj.timestamp);
        let d = {
          name: dobj.stationName,
          value: [
            dobj.timestamp,
            dobj.count
          ]
        };
        if (sData2.length > 5) {
          sData2.shift();
        }
        sData2.push(d);
        console.log(">> " + JSON.stringify(sData2));

        myChart2.setOption({
          series: [
            {
              data: sData2
            }
          ]
        });
      }
    };
  }
}

// random clientId
function generateClientId(length) {
  var result = '';
  var characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  var charactersLength = characters.length;
  for (var i = 0; i < length; i++) {
    result += characters.charAt(Math.floor(Math.random() * charactersLength));
  }
  return result;
}

// Initialize the echarts instance based on the prepared dom
// function randomData() {
//   now = new Date(+now + oneDay);
//   value = value + Math.random() * 21 - 10;
//   return {
//     name: now.toString(),
//     value: [
//       [now.getFullYear(), now.getMonth() + 1, now.getDate()].join('/'),
//       Math.round(value)
//     ]
//   };
// }

let data = [];
let now = new Date();
let oneDay = 24 * 3600 * 1000;
let value = Math.random() * 1000;
//for (var i = 0; i < 1000; i++) {
//  data.push(randomData());
//}

var option = {
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
      params = params[0];
      var date = new Date(params.value[0]);
      return (
        date.getHours() +
        ':' +
        (date.getMinutes()<10?'0':'') + date.getMinutes() +
        ':' +
        date.getSeconds() +
        ' ' +
        date.getDate() +
        '/' +
        (date.getMonth() + 1) +
        '/' +
        date.getFullYear() +
        ' : ' +
        params.value[1]
      );
    },
    axisPointer: {
      animation: false
    }
  },
  xAxis: {
    type: 'time',
    splitLine: {
      show: false
    },
    axisLabel: {
      formatter: (function (value) {
        return echarts.format.formatTime('hh:mm:ss', value);
      })
    }
  },
  yAxis: {
    type: 'value',
    boundaryGap: [0, '100%'],
    splitLine: {
      show: false
    },
    min: function (value) {
      return value.min - 10;
    }
  },
  series: [
    {
      name: 'Fake Data',
      type: 'line',
      showSymbol: false,
      data: data,
      itemStyle: { normal: { areaStyle: { type: 'default' } } },
    }
  ]
};

//setInterval(function () {
//  for (var i = 0; i < 5; i++) {
//    data.shift();
//    var rd = randomData();
//    //console.log(rd);
//    data.push(rd);
//  }
//  myChart.setOption({
//    series: [
//      {
//        data: data
//      }
//    ]
//  });
//}, 400);

option && myChart1.setOption(option);
option && myChart2.setOption(option);
