
(ns docs-generator.read.engine
    (:require [docs-generator.import.state :as import.state]
              [docs-generator.read.env     :as read.env]
              [docs-generator.read.state   :as read.state]
              [docs-generator.read.utils   :as read.utils]
              [fruits.regex.api            :as regex]
              [fruits.string.api           :as string]
              [fruits.vector.api           :as vector]
              [io.api                      :as io]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-function-warning
  ; @ignore
  ;
  ; @param (string) header
  ;
  ; @usage
  ; (read-function-warning "@warning ...")
  ; =>
  ; "..."
  ;
  ; @return (string)
  [header]
  (if (string/contains-part? header "@warning")
      (read.utils/function-warning header)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-function-description
  ; @ignore
  ;
  ; @param (string) header
  ;
  ; @usage
  ; (read-function-description "@description ...")
  ; =>
  ; "..."
  ;
  ; @return (string)
  [header]
  (if (string/contains-part? header "@description")
      (read.utils/function-description header)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-function-return
  ; @ignore
  ;
  ; @param (string) header
  ;
  ; @usage
  ; (read-function-return "... @return (map) {:my-key (string)}")
  ; =>
  ; {"sample" {:my-key (string)}
  ;  "types" "map"}
  ;
  ; @return (map)
  ; {"sample" (string)
  ;  "types" (string)}
  [header]
  (if (string/contains-part? header "@return")
      (read.utils/function-return header)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-function-first-usage
  ; @ignore
  ;
  ; @param (string) header
  ; @param (integer) cursor
  ;
  ; @usage
  ; (read-function-first-usage "... ; @usage (my-function ...) ..." 42)
  ; =>
  ; {"call" "(my-function ...)"}
  ;
  ; @return (map)
  ; {"call" (string)}
  [header cursor]
  (-> header (string/keep-range cursor)
             (read.utils/function-first-usage)))

(defn read-function-usages
  ; @ignore
  ;
  ; @param (string) header
  ;
  ; @usage
  ; (read-function-usages "... ; @usage (my-function ...) ..." 42)
  ; =>
  ; [{"call" "(my-function ...)"}]
  ;
  ; @return (maps in vector)
  ; [{"call" (string)}
  ;  {...}]
  [header]
  (letfn [(f0 [usages skip]
              (if-let [cursor (string/nth-dex-of header "  ; @usage" skip)]
                      (let [usage (read-function-first-usage header cursor)]
                           (f0 (conj usages usage)
                               (inc  skip)))
                      (-> usages)))]
         (f0 [] 0)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-function-first-example
  ; @ignore
  ;
  ; @param (string) header
  ; @param (integer) cursor
  ;
  ; @usage
  ; (read-function-first-example "... ; @example (my-function ...) => 123 ..." 42)
  ; =>
  ; {"call" "(my-function ...)" "result" 123}
  ;
  ; @return (map)
  ; {"call" (string)
  ;  "result" (string)}
  [header cursor]
  (-> header (string/keep-range cursor)
             (read.utils/function-first-example)))

(defn read-function-examples
  ; @ignore
  ;
  ; @param (string) header
  ;
  ; @usage
  ; (read-function-examples "... ; @example (my-function ...) => 123 ..." 42)
  ; =>
  ; [{"call" "(my-function ...)" "result" 123}]
  ;
  ; @return (maps in vector)
  ; [{"call" (string)
  ;   "result" (string)}
  ;  {...}]
  [header]
  (letfn [(f0 [examples skip]
              (if-let [cursor (string/nth-dex-of header "  ; @example" skip)]
                      (let [example (read-function-first-example header cursor)]
                           (f0 (conj examples example)
                               (inc  skip)))
                      (-> examples)))]
         (f0 [] 0)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-function-first-param
  ; @ignore
  ;
  ; @param (string) header
  ; @param (integer) cursor
  ;
  ; @usage
  ; (read-function-first-param "... ; @param (*)(opt) my-param ..." 42)
  ; =>
  ; {"name" "my-param" "optional?" true "types" "*"}
  ;
  ; @return (map)
  ; {"name" (string)
  ;  "optional?" (boolean)
  ;  "sample" (string)
  ;  "types" (string)}}
  [header cursor]
  (-> header (string/keep-range cursor)
             (read.utils/function-first-param)))

(defn read-function-params
  ; @ignore
  ;
  ; @param (string) header
  ;
  ; @usage
  ; (read-function-params "...")
  ; =>
  ; (?)
  ;
  ; @return (maps in vector)
  ; [{"name" (string)
  ;   "optional?" (boolean)
  ;   "sample" (string)
  ;   "types" (string)}
  ;  {...}]
  [header]
  (letfn [(f0 [params skip]
              (if-let [cursor (string/nth-dex-of header "  ; @param" skip)]
                      (let [param (read-function-first-param header cursor)]
                           (f0 (conj params param)
                               (inc  skip)))
                      (-> params)))]
         (f0 [] 0)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-function-header
  ; @ignore
  ;
  ; @param (string) header
  ;
  ; @usage
  ; (read-function-header "...")
  ; =>
  ; (?)
  ;
  ; @return (map)
  ; {"description" (string)
  ;  "examples" (maps in vector)
  ;  "params" (maps in vector)
  ;  "return" (map)
  ;  "usages" (maps in vector)
  ;  "warning" (string)}
  [header]
  {"description" (read-function-description header)
   "warning"     (read-function-warning     header)
   "params"      (read-function-params      header)
   "examples"    (read-function-examples    header)
   "usages"      (read-function-usages      header)
   "return"      (read-function-return      header)})

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-constant
  ; @ignore
  ;
  ; @param (string) file-content
  ; @param (string) name
  ; @param (string) redirected-to
  ;
  ; @usage
  ; (read-constant "..." "MY-CONSTANT" "MY-CONSTANT")
  ; =>
  ; {"type" ["string"]}
  ;
  ; @return (map)
  ; {"name" (string)
  ;  "type" (strings in vector)}
  [file-content name redirected-to]
  ; pattern: "(def MY-CONSTANT"
  (let [pattern (str "[(]def[ ]+"name)]
       (if-let [start-pos (regex/first-dex-of file-content (re-pattern pattern))]
               {"name" name})))

(defn read-function
  ; @ignore
  ;
  ; @param (string) file-content
  ; @param (string) name
  ; @param (string) redirected-to
  ;
  ; @usage
  ; (read-function "..." "my-function" "my-function")
  ; =>
  ; (?)
  ;
  ; @return (map)
  ; {"header" (map)
  ;   {"examples" (maps in vector)
  ;    "params" (maps in vector)
  ;    "return" (map)
  ;    "usages" (maps in vector)}
  ;  "name" (string)}
  [file-content name redirected-to]
  (if-let [header (read.utils/function-header file-content redirected-to)]
          {"header" (read-function-header header)
           "code"   (read.utils/function-code file-content redirected-to)
           "name"   name}
          {"code"   (read.utils/function-code file-content redirected-to)
           "name"   name}))

(defn read-code
  ; @ignore
  ;
  ; @param (string) file-content
  ; @param (string) name
  ; @param (string) redirected-to
  ;
  ; @usage
  ; (read-code "..." "my-function" "my-function")
  ; =>
  ; {"function" {...}}
  ;
  ; @usage
  ; (read-code "..." "MY-CONSTANT" "MY-CONSTANT")
  ; =>
  ; {"constant" {...}}
  ;
  ; @return (map)
  ; {"constant" (map)
  ;   {"type" (strings in vector)}
  ;  "function" (map)
  ;   {"header" (map)
  ;     {"examples" (maps in vector)
  ;      "params" (maps in vector)
  ;      "return" (map)
  ;      "usages" (maps in vector)}}}
  [file-content name redirected-to]
  (let [function-data (read-function file-content name redirected-to)
        constant-data (read-constant file-content name redirected-to)]
       (if-let [function-code (get function-data "code")]
               {"function" function-data}
               (if-let [constant-code (get constant-data "code")]
                       {"constant" constant-data}))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-def
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ; @param (string) name
  ; @param (string) value
  ;
  ; @usage
  ; (read-def {...} "clj" "my-repository/source-code/my_directory/api.clj" "my-function" "my-namespace/my-function")
  ; =>
  ; {"function" {...}}
  ;
  ; @usage
  ; (read-def {...} "clj" "my-repository/source-code/my_directory/api.clj" "MY-CONSTANT" "my-namespace/MY-CONSTANT")
  ; =>
  ; {"constant" {...}}
  ;
  ; @return (map)
  ; {"constant" (map)
  ;  "function" (map)}
  [options layer-name api-filepath name value]
  ; redirected-to:
  ; Az egyes függvények és konstansok az api fájlokban nem feltétlenül egy ugyanolyan
  ; nevű függvényre vagy konstansra vannak átirányítva!
  (let [alias (or (string/before-first-occurence value "/" {:return? false})
                  (get-in @import.state/LAYERS [layer-name api-filepath "refers" value]))
        redirected-to (string/after-first-occurence value "/" {:return? true})
        code-filepath (read.env/code-filepath options layer-name api-filepath alias)]
       (if (io/file-exists? code-filepath)
           (let [file-content (io/read-file code-filepath)]

                (read-code file-content name redirected-to))

           ; Ha a code-filepath útvonalon nem olvasható be forráskód fájl tartalma,
           ; akkor megpróbálja a cljc fájlként elérni a fájlt, mert előfordulhat,
           ; hogy egy clj vagy cljs api fájl közvetlenül egy cljc fájlból
           ; irányít át függvényeket vagy konstansokat.
           (if-let [alter-filepath (read.env/alter-filepath options layer-name api-filepath alias)]
                   (let [file-content (io/read-file alter-filepath)]
                        (read-code file-content name redirected-to))))))

(defn read-defs
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ;
  ; @usage
  ; (read-defs {...} "clj" "my-repository/source-code/my_directory/api.clj")
  ; =>
  ; {"constants" [{...}]
  ;  "functions" [{...}]}
  ;
  ; @return (map)
  ; {"constants" (maps in vector)
  ;  "functions" (maps in vector)}
  [options layer-name api-filepath]
  (let [defs (get-in @import.state/LAYERS [layer-name api-filepath "defs"])]
       (letfn [(f0 [result [name value :as def]]
                   (let [def (read-def options layer-name api-filepath name value)]
                        (if-let [function-data (get def "function")]
                                (update result "functions" vector/conj-item function-data)
                                (if-let [constant-data (get def "constant")]
                                        (update result "constants" vector/conj-item constant-data)
                                        (->     result)))))]
              (reduce f0 {} defs))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-api-file
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ;
  ; @usage
  ; (read-api-file {...} "clj" "my-repository/source-code/my_directory/api.clj")
  ; =>
  ; {"constants" [{...}]
  ;  "functions" [{...}]}
  ;
  ; @return (map)
  ; {"constants" (maps in vector)
  ;  "functions" (maps in vector)}
  [options layer-name api-filepath]
  (read-defs options layer-name api-filepath))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-layer
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) layer-name
  ;
  ; @usage
  ; (read-layer {...} "clj")
  ; =>
  ; {"my-repository/source-code/my_directory/api.clj" {...}
  ;  "..." {...}}
  ;
  ; @return (map)
  [options layer-name]
  (let [layer-data (get @import.state/LAYERS layer-name)]
       (letfn [(f0 [layer-data api-filepath api-data]
                   (assoc layer-data api-filepath (read-api-file options layer-name api-filepath)))]
              (reduce-kv f0 {} layer-data))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-layers!
  ; @ignore
  ;
  ; @param (map) options
  [options]
  (let [layers @import.state/LAYERS]
       (letfn [(f0 [_ layer-name _]
                   (let [layer-data (read-layer options layer-name)]
                        (swap! read.state/LAYERS assoc layer-name layer-data)))]
              (reduce-kv f0 nil layers))))
