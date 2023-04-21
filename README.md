
# clj-docs-generator

### Overview

The <strong>clj-docs-generator</strong> is a documentation book generator for Clojure/ClojureScript
projects.

### Warning

The <strong>clj-docs-generator</strong> library is in pre-beta stage.
Not recommended to use in product releases!

# Usage

> Some parameters of the following functions and some other functions won't be discussed.
  To learn more about the available functionality, check out the
  [functional documentation](documentation/COVER.md)!

### Index

- [Expected syntax](#expected-syntax)

- [How to generate documentation books?](#how-to-generate-documentation-books)

### Expected syntax

The <strong>clj-docs-generator</strong> documentation book generator can read Clojure
functions written with the following header syntax:

```
(defn my-function
  ; @warning
  ; This is a very useful sample function! Use carefully!
  ;
  ; @description
  ; Returns with the given parameters in a map.
  ;
  ; @param (string)(opt) a
  ; @param (map) b
  ; {:c (string)(opt)}
  ;
  ; @usage
  ; (my-function {...})
  ;
  ; @usage
  ; (my-function "..." {...})
  ;
  ; @example
  ; (my-function "A" {:c "C"})
  ; =>
  ; {:a "A" :b {:c "C"}}
  ;
  ; @example
  ; (my-function "A" "B")
  ; =>
  ; nil
  ;
  ; @return (map)
  ; {:a (string)
  ;  :b (map)}
  ([b]
   (my-function nil b)

  ([a b]
   (and (string? a)
        (map?    b)
        {:a a :b b}))
```

### How to generate documentation books?

The [`docs-generator.api/create-documentation!`](documentation/clj/docs/API.md#create-documentation)
function reads the files from the `code-dirs` folders, creates the documentation
and prints the markdown files to the `output-dir` folder.

> The create-documentation! function erases the output-dir before printing
  the documentation books!

```
(create-documentation! {:code-dirs  ["src/clj" "src/cljc" "src/cljs"]
                        :lib-name   "My library"
                        :output-dir "documentation"})
```

You can set the author name and website printed to books.

```
(create-documentation! {:author     "Author"
                        :code-dirs  ["src/clj" "src/cljc" "src/cljs"]
                        :lib-name   "My library"
                        :output-dir "documentation"
                        :website    "httsp://github.com/author/my-repository"})
```

By using the `:abs-path "..."` setting you can specify where your code placed in
the project directory. It might be useful if you want to create documentation books
for submodules or subprojects.

```
(create-documentation! {:abs-path   "submodules/my-repository"
                        :code-dirs  ["src/clj" "src/cljc" "src/cljs"]
                        :lib-name   "My library"
                        :output-dir "documentation")
```

By using the `:print-options [...]` setting you can specify what kind of information
you want to be printed into the books.

```
(create-documentation! {:code-dirs  ["src/clj" "src/cljc" "src/cljs"]
                        :lib-name   "My library"
                        :output-dir "documentation"
                        :print-options
                        [:code :description :examples :params :require :return :usages :warning])
```
