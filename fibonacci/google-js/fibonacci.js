'use strict';

const uuidv4 = require('uuid/v4');
const containerId = uuidv4();

exports.HANDLER = (req, res) => {
    var n = req.body.n;
    
    var requestId = uuidv4();

    if (!n || !isNumeric(n)) {
        res.status(400).send(createResponse("The parameter is not valid", requestId));
        return;
    }

    n = parseInt(n);
    var result = fib(n);
    console.log("SEMODE::" + createResponse(result, requestId));
    res.status(200).send(createResponse(result, requestId));
};

var fib = function(n) {
    if (n <= 1) {
        return 1;
    } else {
        return fib(n - 1) + fib(n - 2);
    }
};

var createResponse = function (message, requestId) {
    var response = {
        result: message,
        platformId: requestId,
        containerId: containerId
    };

    return JSON.stringify(response);
};

var isNumeric = function(value) {
    return /^\d+$/.test(value);
};
