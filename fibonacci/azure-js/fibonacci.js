'use strict';

/* eslint-disable no-param-reassign */
const uuidv4 = require('uuid/v4');
const containerId = uuidv4();

module.exports.handler = function (context, req) {
    var n;

    if (req.query && req.query.n) {
        n = req.query.n;
    }
    else if (req.body && req.body.n) {
        n = req.body.n;
    }

    if (!n || !isNumeric(n)) {
        context.res = createErrorResponse("Please pass a valid number 'n'.", context.invocationId);
        context.done();
        return;
    }

    n = parseInt(n);
    var result = fib(n);

    context.res = createSuccessResponse(result, context.invocationId);
    context.done();
};

var fib = function(n) {
    if (n <= 1) {
        return 1;
    } else {
        return fib(n - 1) + fib(n - 2);
    }
};

var createErrorResponse = function (message, platformId) {
    return createResponse(400, message, platformId);
};

var createSuccessResponse = function (message, platformId) {
    return createResponse(200, message, platformId);
};

var createResponse = function (status, message, platformId) {
    var response = {
        result: message,
        platformId: platformId,
        containerId: containerId
    };

    return {
        status: status,
        body: response,
    }
};

var isNumeric = function(value) {
    return /^\d+$/.test(value);
};
