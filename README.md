# webapp-tools
Miscellaneous tools one might find useful for Scala-based webapp development.

## Making a Release ##
After ensuring the test-suite passes (`sbt "+ test"`), run the following two
commands on the `sbt` console:
```
+ publishSigned
sonatypeRelease
```