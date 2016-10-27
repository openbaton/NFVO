/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

// First Chart Example - Area Line Chart

Morris.Area({
  // ID of the element in which to draw the chart.
  element: 'morris-chart-area',
  // Chart data records -- each entry in this array corresponds to a point on
  // the chart.
  data: [
	{ d: '2014-07-01', visits: 0 },
	{ d: '2014-07-02', visits: 0 },
	{ d: '2014-07-03', visits:  0 },
	{ d: '2014-07-04', visits: 0 },
	{ d: '2014-07-05', visits: 0 },
	{ d: '2014-07-06', visits: 0 },
	{ d: '2014-07-07', visits: 0 },
	{ d: '2014-07-08', visits: 0 },
	{ d: '2014-07-09', visits: 0 },
	{ d: '2014-07-10', visits: 0 },
	{ d: '2014-07-11', visits: 0 },
	{ d: '2014-07-12', visits: 0 },
	{ d: '2014-07-13', visits: 0 },
	{ d: '2014-07-14', visits: 0 },
	{ d: '2014-07-15', visits: 0 },
	{ d: '2014-07-16', visits: 0 },
	{ d: '2014-07-17', visits: 0 },
	{ d: '2014-07-18', visits: 0 },
	{ d: '2014-07-19', visits: 0 },
	{ d: '2014-07-20', visits: 0 },
	{ d: '2014-07-21', visits: 0 },
	{ d: '2014-07-22', visits: 0 },
	{ d: '2014-07-23', visits: 0 },
	{ d: '2014-07-24', visits: 0 },
	{ d: '2014-07-25', visits: 0 },
	{ d: '2014-07-26', visits: 0 },
	{ d: '2014-07-27', visits: 0 },
	{ d: '2014-07-28', visits: 0 },
	{ d: '2014-07-29', visits: 0 },
	{ d: '2014-07-30', visits: 0 },
	{ d: '2014-07-31', visits: 0 },
  ],
  // The name of the data record attribute that contains x-visitss.
  xkey: 'd',
  // A list of names of data record attributes that contain y-visitss.
  ykeys: ['visits'],
  // Labels for the ykeys -- will be displayed when you hover over the
  // chart.
  labels: ['Running'],
  // Disables line smoothing
  smooth: false,
});


