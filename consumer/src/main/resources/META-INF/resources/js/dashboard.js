"use strict"

var connected = false;
var socket;
var myChart = echarts.init(document.getElementById('main'));
let sData = [];

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
      console.log("Got message: " + m.data);
      $("#message").text(m.data);
      let dobj = JSON.parse(m.data);
      if (dobj.stationId == 1) {
        now = new Date(+now + oneDay);
        let d = {
          name: now.toString(),
          value: [
            [now.getFullYear(), now.getMonth() + 1, now.getDate()].join('/'),
            dobj.count
          ]
        };

        if (sData.length > 20) {
          sData.shift;
        }
        sData.push(d);

        console.log(">> " + sData);

        myChart.setOption({
          series: [
            {
              data: sData
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
      var date = new Date(params.name);
      return (
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
      data: data
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

option && myChart.setOption(option);
