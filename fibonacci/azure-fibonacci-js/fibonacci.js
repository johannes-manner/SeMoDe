'use strict';

/* eslint-disable no-param-reassign */

module.exports.handler = function (context, req) {
    var n;

    if (req.query && req.query.n) {
        n = req.query.n;
    }
    else if (req.body && req.body.n) {
        n = req.body.n;
    }

    if (!n || !isNumeric(n)) {
        context.res = createErrorResponse("Please pass a valid number 'n'.");
        context.done();
        return;
    }

    n = parseInt(n);
    var result = fib(n);

    context.res = createSuccessResponse(result);
    context.done();
};

var fib = function(n) {
    if (n <= 1) {
        return 1;
    } else {
        return fib(n - 1) + fib(n - 2);
    }
};

var createErrorResponse = function (message) {
    return {
        status: 400,
        body: message,
    }
};

var createSuccessResponse = function (message) {
    return {
        status: 200,
        body: message,
    }
};

var isNumeric = function(value) {
    return /^\d+$/.test(value);
};
