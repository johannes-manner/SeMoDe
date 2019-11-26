// This script creates a context mock and runs the fibonacci function locally.

const context = require('aws-lambda-mock-context');
const ctx = context();
require('./fibonacci').handler({n:process.env.FIB_INPUT},ctx);
ctx.Promise.then(() => {console.log("success");}).catch(err => {console.log(err);throw err;});
