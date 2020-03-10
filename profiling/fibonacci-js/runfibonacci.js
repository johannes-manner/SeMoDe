// This script creates a context mock and runs the fibonacci function locally.


var isNumeric = function(value) {
    return /^\d+$/.test(value);
};

var fib = function(n) {
    if (n <= 1) {
        return 1;
    } else {
        return fib(n - 1) + fib(n - 2);
    }
};


var n = process.env.FIB_INPUT;

if (!n || !isNumeric(n)) {
    return;
}

n = parseInt(n);
var result = fib(n);
