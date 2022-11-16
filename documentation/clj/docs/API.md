
# <strong>docs.api</strong> namespace
<p>Documentation of the <strong>docs/api.clj</strong> file</p>

<strong>[README](../../../README.md) > [DOCUMENTATION](../../COVER.md) > docs.api</strong>



### create-documentation!

```
@param (map) options
```

```
@usage
 (create-documentation! {...})
```

```
@usage
 (create-documentation! {:path "my-submodules/my-repository"})
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
       (debug)))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [docs.api :as docs :refer [create-documentation!]]))

(docs/create-documentation! ...)
(create-documentation!      ...)
```

</details>
