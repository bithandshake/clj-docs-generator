
# <strong>docs.api</strong> namespace

<strong>[README](../../../README.md) > [DOCUMENTATION](../../COVER.md) > </strong>source-code/clj/docs/api.clj

### create-documentation!

```
@param (map) options
{:abs-path (string)
 :code-dirs (strings in vector)
 :lib-name (string)
 :output-dir (string)}
```

```
@usage
(create-documentation! {...})
```

```
@usage
(create-documentation! {:abs-path   "submodules/my-repository"
                        :code-dirs  ["source-code/clj"]
                        :output-dir "documentation"
                        :lib-name   "my-repository"})
```

```
@return (string)
```

<details>
<summary>Source code</summary>

```
(defn create-documentation!
  [options]
  (let [options (core.prototypes/options-prototype options)]
       (initialize!                    options)
       (detect.engine/detect-layers!   options)
       (import.engine/import-layers!   options)
       (read.engine/read-layers!       options)
       (process.engine/process-layers! options)
       (process.engine/process-cover!  options)
       (print.engine/print-cover!      options)
       (print.engine/print-layers!     options)
       (debug options)))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [docs.api :refer [create-documentation!]]))

(docs.api/create-documentation! ...)
(create-documentation!          ...)
```

</details>
