
# docs-api

### Overview

The <strong>docs-api</strong> is a documentation book generator for Clojure/ClojureScript
libraries.

### Warning

The <strong>docs-api</strong> library is in pre-beta stage.
It is not recommended to use it in product releases!

### How it works?

The <strong>docs-api</strong> documentation book generator can read ClojureScript/ClojureScript
files written with the following syntax:

```
(defn my-function
  ; @warning
  ; It can assoc every type of parameters! Use carefully!
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
  ;
  ; @return (map)
  ; {:a (string)
  ;  :b (map)}
  ([b])
   (my-function nil b)

  ([a b]
   (and (string? a)
        (map?    b)
        {:a a :b b})
```

The `docs.api/create-documentation!` function reads the files from the `source-dirs`
folders, creates the documentation and prints the markdown files to the `output-dir` folder.
