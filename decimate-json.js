'use strict';
var fs = require('fs')
var assert = require('assert')

var rows = JSON.parse(fs.readFileSync('bkg-2.json'))

function processRow(cols)
{
	var x = 0
	var r = []
	r.push(cols[0])
	for (x = 1; x < cols.length; x += 10)
	{
		r.push(cols[x])
	}
	return r
}

function iter(arr, f)
{
	f(arr[0])
	for (var x = 1; x < arr.length; x += 10)
	{
		f(arr[x])
	}
}

var rr = []
iter(rows, function (cols)
{
	var r = []
	iter(cols, function (pix)
	{
		r.push(pix)
	})
	assert.equal(r.length, 55)
	rr.push(r)
})

assert.equal(rr.length, 55)
console.log(JSON.stringify(rr))