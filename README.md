# patterns-pitfalls-liberator

Sample code written for a talk on Liberator

## Usage

The slides can be viewed directly at [http://davidjameshumphreys.github.io/patterns-pitfalls-liberator](http://davidjameshumphreys.github.io/patterns-pitfalls-liberator).

The example code contains some unfinished routes.

To create the database run:
```
lein up
lein seed dev sql-dev
```

To run the server:
```
lein repl
```

Then run `(reset)` from the `user` namespace where the repl should start.

## License

Copyright © 2015 David Humphreys:

Uses reveal.js

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
