
# docs-generator.api Clojure namespace

##### [README](../../../README.md) > [DOCUMENTATION](../../COVER.md) > docs-generator.api

### Index

- [create-documentation!](#create-documentation)

- [debug](#debug)

### create-documentation!

```
@warning
The create-documentation! function erases the output-dir before printing
the documentation books!
Be careful with configuring this function!
```

```
@param (map) options
{:author (string)(opt)
 :code-dirs (strings in vector)
 :filename-pattern (regex pattern)(opt)
 :lib-name (string)
 :output-dir (string)
 :print-options (keywords in vector)(opt)
  [:code, :credits, :description, :examples, :params, :require, :return, :usages, :warning]
  Default: [:code :credits :description :examples :params :require :return :usages :warning]
 :website (string)(opt)}
```

```
@usage
(create-documentation! {...})
```

```
@usage
(create-documentation! {:author           "Author"
                        :code-dirs        ["submodules/my-repository/source-code"]
                        :filename-pattern "[a-z\-]\.clj"
                        :output-dir       "submodules/my-repository/documentation"
                        :lib-name         "My library"
                        :website          "https://github.com/author/my-repository"})
```

```
@return (string)
```

<details>
<summary>Source code</summary>

```
(defn create-documentation!
  [options]
  (if (v/valid? options {:pattern* core.patterns/OPTIONS-PATTERN})
      (let [options (core.prototypes/options-prototype options)]
           (initialize!                    options)
           (detect.engine/detect-layers!   options)
           (import.engine/import-layers!   options)
           (read.engine/read-layers!       options)
           (process.engine/process-layers! options)
           (process.engine/process-cover!  options)
           (process.engine/process-common! options)
           (print.engine/print-cover!      options)
           (print.engine/print-layers!     options)
           (debug))))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [docs-generator.api :refer [create-documentation!]]))

(docs-generator.api/create-documentation! ...)
(create-documentation!                    ...)
```

</details>

---

### debug

```
@usage
(debug)
```

```
@return (string)
```

<details>
<summary>Source code</summary>

```
(defn debug
  []
  (str "<pre style=\"background:#fafafa\">"
       "\n\ndetected layers:\n"  (get-in @detect.state/LAYERS  [])
       "\n\nimported layers:\n"  (get-in @import.state/LAYERS  [])
       "\n\nread layers:\n"      (get-in @read.state/LAYERS    [])
       "\n\nprocessed layers:\n" (get-in @process.state/LAYERS [])
       "\n\nprocessed cover:\n"  (get-in @process.state/COVER  [])
       "\n\nprocessed common:\n" (get-in @process.state/COMMON [])
       "</pre>"))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [docs-generator.api :refer [debug]]))

(docs-generator.api/debug)
(debug)
```

</details>

---

This documentation is generated with the [clj-docs-generator](https://github.com/bithandshake/clj-docs-generator) engine.

