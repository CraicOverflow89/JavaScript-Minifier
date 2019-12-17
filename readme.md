JavaScript Minifier
===================

Lightweight command line tool that utilises the great work of [javascript-minifier.com](https://javascript-minifier.com/java), to minify `js` files.

### Usage

You can minify single files or entire directories and optionally specifiy an alternative output directory.

```
$ jsmin src/my_file.js
# Creates my_file.min.js in the src directory

$ jsmin src/my_file.js out
# Creates my_file.min.js in the out directory

$ jsmin src out
# Minifies all files in src and puts results in out

$ jsmin src out -r
# Same as before including subdirectories
```