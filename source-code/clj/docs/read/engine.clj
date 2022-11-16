
(ns docs.read.engine
    (:require [candy.api         :refer [return]]
              [docs.import.state :as import.state]
              [docs.read.helpers :as read.helpers]
              [docs.read.state   :as read.state]
              [io.api            :as io]
              [regex.api         :as regex]
              [mid-fruits.string :as string]
              [syntax.api        :as syntax]
              [mid-fruits.vector :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-function-return
  ; @param (string) header
  ;
  ; @example
  ;  (read-function-return "... @return (map) {:my-key (string)}")
  ;  =>
  ;  {"sample" {:my-key (string)}
  ;   "types" "map"}
  ;
  ; @return (map)
  ;  {"sample" (string)
  ;   "types" (string)}
  [header]
  (read.helpers/first-return header))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-function-first-usage
  ; @param (string) header
  ; @param (integer) cursor
  ;
  ; @example
  ;  (read-function-first-usage "... ; @usage (my-function ...) ..." 42)
  ;  =>
  ;  {"call" "(my-function ...)"}
  ;
  ; @return (map)
  ;  {"call" (string)}
  [header cursor]
  (-> header (string/part cursor)
             (read.helpers/first-usage)))

(defn read-function-usages
  ; @param (string) header
  ;
  ; @example
  ;  (read-function-usages "... ; @usage (my-function ...) ..." 42)
  ;  =>
  ;  [{"call" "(my-function ...)"}]
  ;
  ; @return (maps in vector)
  ;  [{"call" (string)}
  ;   {...}]
  [header]
  (letfn [(f [usages n]
             (if-let [cursor (string/nth-dex-of header "  ; @usage" n)]
                     (let [usage (read-function-first-usage header cursor)]
                          (f (conj usages usage)
                             (inc n)))
                     (return usages)))]
         (f [] 1)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-function-first-example
  ; @param (string) header
  ; @param (integer) cursor
  ;
  ; @example
  ;  (read-function-first-example "... ; @example (my-function ...) => 123 ..." 42)
  ;  =>
  ;  {"call" "(my-function ...)" "result" 123}
  ;
  ; @return (map)
  ;  {"call" (string)
  ;   "result" (string)}
  [header cursor]
  (-> header (string/part cursor)
             (read.helpers/first-example)))

(defn read-function-examples
  ; @param (string) header
  ;
  ; @example
  ;  (read-function-examples "... ; @example (my-function ...) => 123 ..." 42)
  ;  =>
  ;  [{"call" "(my-function ...)" "result" 123}]
  ;
  ; @return (maps in vector)
  ;  [{"call" (string)
  ;    "result" (string)}
  ;   {...}]
  [header]
  (letfn [(f [examples n]
             (if-let [cursor (string/nth-dex-of header "  ; @example" n)]
                     (let [example (read-function-first-example header cursor)]
                          (f (conj examples example)
                             (inc n)))
                     (return examples)))]
         (f [] 1)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-function-first-param
  ; @param (string) header
  ; @param (integer) cursor
  ;
  ; @example
  ;  (read-function-first-param "... ; @param (*)(opt) my-param ..." 42)
  ;  =>
  ;  {"name" "my-param" "optional?" true "types" "*"}
  ;
  ; @return (map)
  ;  {"name" (string)
  ;   "optional?" (boolean)
  ;   "sample" (string)
  ;   "types" (string)}}
  [header cursor]
  (-> header (string/part cursor)
             (read.helpers/first-param)))

(defn read-function-params
  ; @param (string) header
  ;
  ; @example
  ;  (read-function-params "...")
  ;  =>
  ;  (?)
  ;
  ; @return (maps in vector)
  ;  [{"name" (string)
  ;    "optional?" (boolean)
  ;    "sample" (string)
  ;    "types" (string)}
  ;   {...}]
  [header]
  (letfn [(f [params n]
             (if-let [cursor (string/nth-dex-of header "  ; @param" n)]
                     (let [param (read-function-first-param header cursor)]
                          (f (conj params param)
                             (inc n)))
                     (return params)))]
         (f [] 1)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-function-header
  ; @param (string) header
  ;
  ; @example
  ;  (read-function-header "...")
  ;  =>
  ;  (?)
  ;
  ; @return (map)
  ;  {"examples" (maps in vector)
  ;   "params" (maps in vector)
  ;   "return" (map)
  ;   "usages" (maps in vector)}
  [header]
  {"params"   (read-function-params   header)
   "examples" (read-function-examples header)
   "usages"   (read-function-usages   header)
   "return"   (read-function-return   header)})

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-constant
  ; @param (string) file-content
  ; @param (string) name
  ;
  ; @example
  ;  (read-constant "..." "MY-CONSTANT")
  ;  =>
  ;  {"type" ["string"]}
  ;
  ; @return (map)
  ;  {"name" (string)
  ;   "type" (strings in vector)}
  [file-content name]
  ; pattern: "(def MY-CONSTANT"
  (let [pattern (str "[(]def[ ]{1,}"name)]
       (if-let [start-pos (regex/first-dex-of file-content (re-pattern pattern))]
               {"name" name})))

(defn read-function
  ; @param (string) file-content
  ; @param (string) name
  ;
  ; @example
  ;  (read-function "..." "my-function")
  ;  =>
  ;  (?)
  ;
  ; @return (map)
  ;  {"header" (map)
  ;    {"examples" (maps in vector)
  ;     "params" (maps in vector)
  ;     "return" (map)
  ;     "usages" (maps in vector)}
  ;   "name" (string)}
  [file-content name]
  (if-let [header (read.helpers/function-header file-content name)]
          {"header" (read-function-header header)
           "code"   (read.helpers/function-code file-content name)
           "name"   name}
          {"code"   (read.helpers/function-code file-content name)
           "name"   name}))

(defn read-code
  ; @param (string) file-content
  ; @param (string) name
  ;
  ; @example
  ;  (read-code "..." "my-function")
  ;  =>
  ;  {"function" {...}}
  ;
  ; @example
  ;  (read-code "..." "MY-CONSTANT")
  ;  =>
  ;  {"constant" {...}}
  ;
  ; @return (map)
  ;  {"constant" (map)
  ;    {"type" (strings in vector)}
  ;   "function" (map)
  ;    {"header" (map)
  ;      {"examples" (maps in vector)
  ;       "params" (maps in vector)
  ;       "return" (map)
  ;       "usages" (maps in vector)}}}
  [file-content name]
  (if-let [function (read-function file-content name)]
          {"function" function}
          (if-let [constant (read-constant file-content name)]
                  {"constant" constant})))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-def
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ; @param (string) name
  ; @param (string) value
  ;
  ; @example
  ;  (read-def {:path "my-submodules/my-repository"} "clj" "my-directory" "my-function" "my-namespace/my-function")
  ;  =>
  ;  {"function" {...}}
  ;
  ; @example
  ;  (read-def {:path "my-submodules/my-repository"} "clj" "my-directory" "MY-CONSTANT" "my-namespace/MY-CONSTANT")
  ;  =>
  ;  {"constant" {...}}
  ;
  ; @return (map)
  ;  {"constant" (map)
  ;   "function" (map)}
  [options layer-name directory-name name value]
  (let [alias (or (string/before-first-occurence value "/" {:return? false})
                  (get-in @import.state/LAYERS [layer-name directory-name "refers" value]))
        code-filepath (read.helpers/code-filepath options layer-name directory-name alias)]
       (if (io/file-exists? code-filepath)
           (let [file-content (io/read-file code-filepath)]
                (read-code file-content name))
           ; Ha a code-filepath útvonalon nem olvasható be forráskód fájl tartalma,
           ; akkor megpróbálja a cljc rétegben elérni a fájlt, mert előfordulhat,
           ; hogy a clj vagy cljs réteg api fájlja közvetlenül a cljc rétegből
           ; irányít át függvényeket vagy konstansokat.
           (let [code-filepath (read.helpers/code-filepath options "cljc" directory-name alias)]
                (if (io/file-exists? code-filepath)
                    (let [file-content (io/read-file code-filepath)]
                         (read-code file-content name)))))))

(defn read-defs
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ;
  ; @example
  ;  (read-defs {:path "my-submodules/my-repository"} "clj" "my-directory")
  ;  =>
  ;  {"constants" [{...}]
  ;   "functions" [{...}]}
  ;
  ; @return (map)
  ;  {"constants" (maps in vector)
  ;   "functions" (maps in vector)}
  [options layer-name directory-name]
  (let [defs (get-in @import.state/LAYERS [layer-name directory-name "defs"])]
       (letfn [(f [result [name value :as def]]
                  (let [def (read-def options layer-name directory-name name value)]
                       (if-let [function (get def "function")]
                               (update result "functions" vector/conj-item function)
                               (if-let [constant (get def "constant")]
                                       (update result "constants" vector/conj-item constant)
                                       (return result)))))]
              (reduce f {} defs))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-directory
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ;
  ; @example
  ;  (read-directory {:path "my-submodules/my-repository"} "clj" "my-directory")
  ;  =>
  ;  {"constants" [{...}]
  ;   "functions" [{...}]}
  ;
  ; @return (map)
  ;  {"constants" (maps in vector)
  ;   "functions" (maps in vector)}
  [options layer-name directory-name]
  (read-defs options layer-name directory-name))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-layer
  ; @param (map) options
  ; @param (string) layer-name
  ;
  ; @example
  ;  (read-layer {:path "my-submodules/my-repository"} "clj")
  ;  =>
  ;  {"my-directory" {}}
  ;
  ; @return (map)
  [options layer-name]
  (let [layer-data (get @import.state/LAYERS layer-name)]
       (letfn [(f [layer-data directory-name directory-data]
                  (assoc layer-data directory-name (read-directory options layer-name directory-name)))]
              (reduce-kv f {} layer-data))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn read-layers!
  ; @param (map) options
  [options]
  (let [layers @import.state/LAYERS]
       (letfn [(f [_ layer-name _]
                  (let [layer-data (read-layer options layer-name)]
                       (swap! read.state/LAYERS assoc layer-name layer-data)))]
              (reduce-kv f nil layers))))
