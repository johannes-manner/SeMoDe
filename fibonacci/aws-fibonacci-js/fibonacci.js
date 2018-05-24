'use strict';

module.exports.handler = (event, context, callback) => {
    var n;

    if (event.queryStringParameters && event.queryStringParameters.n) {
        n = event.queryStringParameters.n;
    }
    else if (event.body) {
        try {
            var body = JSON.parse(event.body);

            if (body.n) {
                n = body.n;
            }
        } catch (e) {
            callback(null, createErrorResponse("Please pass valid JSON as body."));
            return;
        }
    }

    if (!n || !isNumeric(n)) {
        callback(null, createErrorResponse("Please pass a valid number 'n'."));
        return;
    }

    n = parseInt(n);
    var result = fib(n);

    callback(null, createSuccessResponse(result));
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
        statusCode: 400,
        body: message,
    }
};

var createSuccessResponse = function (message) {
    return {
        statusCode: 200,
        body: message,
    }
};

var isNumeric = function(value) {
    return /^\d+$/.test(value);
};
