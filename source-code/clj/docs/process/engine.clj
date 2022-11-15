
(ns docs.process.engine
    (:require [candy.api            :refer [return]]
              [docs.process.helpers :as process.helpers]
              [docs.process.state   :as process.state]
              [docs.read.state      :as read.state]
              [mid-fruits.string    :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-function-params
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ; @param (map) function-data
  ;  {"header"
  ;    {"params" (map)(opt)
  ;      {"name" (string)
  ;      {"optional?" (boolean)
  ;      {"types" (string)}}}
  ;
  ; @example
  ;  (process-function-params {:path "my-submodules/my-repository"} "clj" "my-directory" {...})
  ;  =>
  ;  ["@param (string)(opt) my-param"]
  ;
  ; @return (strings in vector)
  [_ _ _ function-data]
  (if-let [params (get-in function-data ["header" "params"])]
          (letfn [(f [params param]
                     (let [name      (get param "name")
                           optional? (get param "optional?")
                           types     (get param "types")]
                          (conj params (str "@param ("types")"
                                            (if optional? "(opt)")
                                            " "name))))]
                 (reduce f [] params))))

(defn process-function-usages
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ; @param (map) function-data
  ;  {"header"
  ;    {"usages" (maps in vector)(opt)
  ;      [{"call" (string)}]}}
  ;
  ; @example
  ;  (process-function-usages {:path "my-submodules/my-repository"} "clj" "my-directory" {...})
  ;  =>
  ;  ["\n@usage ..."]
  ;
  ; @return (strings in vector)
  [_ _ _ function-data]
  (if-let [usages (get-in function-data ["header" "usages"])]
          (letfn [(f [usages usage]
                     (let [call (get usage "call")]
                          (conj usages (str "\n@usage"call))))]
                 (reduce f [] usages))))

(defn process-function-examples
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ; @param (map) function-data
  ;  {"header"
  ;    {"examples" (maps in vector)(opt)
  ;      [{"call" (string)}]}}
  ;
  ; @example
  ;  (process-function-examples {:path "my-submodules/my-repository"} "clj" "my-directory" {...})
  ;  =>
  ;  ["\n@example ..."]
  ;
  ; @return (?)
  [_ _ _ function-data]
  (if-let [examples (get-in function-data ["header" "examples"])]
          (letfn [(f [examples example]
                     (let [call   (get example "call")
                           result (get example "result")]
                          (conj examples (str "\n@example"call"=>"result))))]
                 (reduce f [] examples))))

(defn process-function-return
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ; @param (map) function-data
  ;  {"header"
  ;    {"return" (map)(opt)
  ;      {"types" (string)}}}
  ;
  ; @example
  ;  (process-function-return {:path "my-submodules/my-repository"} "clj" "my-directory" {...})
  ;  =>
  ;  "@return(string)"
  ;
  ; @return (string)
  [_ _ _ function-data]
  (if-let [types (get-in function-data ["header" "return" "types"])]
          (str "@return ("types")")))

(defn process-function-header
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ; @param (map) function-data
  ;
  ; @example
  ;  (process-function-header {:path "my-submodules/my-repository"} "clj" "my-directory" {...})
  ;  =>
  ;  (?)
  ;
  ; @return (map)
  ;  {"examples" (strings in vector)
  ;   "params" (strings in vector)
  ;   "return" (string)
  ;   "usages" (strings in vector)}
  [options layer-name directory-name function-data]
  {"examples" (process-function-examples options layer-name directory-name function-data)
   "params"   (process-function-params   options layer-name directory-name function-data)
   "return"   (process-function-return   options layer-name directory-name function-data)
   "usages"   (process-function-usages   options layer-name directory-name function-data)})

(defn process-function
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ; @param (map) function-data
  ;
  ; @example
  ;  (process-function {:path "my-submodules/my-repository"} "clj" "my-directory" {...})
  ;  =>
  ;  (?)
  ;
  ; @return (map)
  ;  {"header" (map)
  ;   "name" (string)}
  [options layer-name directory-name function-data]
  {"header" (process-function-header options layer-name directory-name function-data)
   "name"   (get function-data "name")})

(defn process-functions
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ;
  ; @example
  ;  (process-functions {:path "my-submodules/my-repository"} "clj" "my-directory")
  ;  =>
  ;  {}
  ;
  ; @return (map)
  [options layer-name directory-name]
  (let [functions (get-in @read.state/LAYERS [layer-name directory-name "functions"])]
       (letfn [(f [result function-data]
                  (conj result (process-function options layer-name directory-name function-data)))]
              (reduce f [] functions))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-directory
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ;
  ; @example
  ;  (process-directory {:path "my-submodules/my-repository"} "clj" "my-directory")
  ;  =>
  ;  {}
  ;
  ; @return (map)
  ;  {}
  [options layer-name directory-name]
  {;"constants" (process-constants options layer-name directory-name)
   "functions" (process-functions options layer-name directory-name)})

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-layer
  ; @param (map) options
  ; @param (string) layer-name
  ;
  ; @example
  ;  (process-layer {:path "my-submodules/my-repository"} "clj")
  ;  =>
  ;  {"my-directory" {}}
  ;
  ; @return (map)
  [options layer-name]
  (let [layer-data (get @read.state/LAYERS layer-name)]
       (letfn [(f [layer-data directory-name directory-data]
                  (assoc layer-data directory-name (process-directory options layer-name directory-name)))]
              (reduce-kv f {} layer-data))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-layers!
  ; @param (map) options
  [options]
  (let [layers @read.state/LAYERS]
       (letfn [(f [_ layer-name _]
                  (let [layer-data (process-layer options layer-name)]
                       (swap! process.state/LAYERS assoc layer-name layer-data)))]
              (reduce-kv f nil layers))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-cover-description
  ; @param (map) options
  ;
  ; @example
  ;  (process-cover-description {...})
  ;  =>
  ;  "..."
  ;
  ; @return (string)
  [_]
  (str "<p>This documentation is generated by the <strong>docs-api</strong> engine</p>"))

(defn process-layer-links
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (strings in vector) links
  ;
  ; @example
  ;  (process-layer-links {...} "clj" [])
  ;  =>
  ;  [... "* [my-directory/api.clj](clj/my-directory/API.md)" ...]
  ;
  ; @return (strings in vector)
  [_ layer-name links]
  (let [layer-data (get @read.state/LAYERS layer-name)]
       (letfn [(f [links directory-name _]
                  (conj links (str "* ["directory-name".api]("layer-name"/"directory-name"/API.md) ["layer-name"]")))]
              (reduce-kv f links layer-data))))

(defn process-cover-links
  ; @param (map) options
  ;
  ; @example
  ;  (process-layer-links {...})
  ;  =>
  ;  ["* [my-directory/api.clj](clj/my-directory/API.md)"
  ;   "* [my-directory/api.cljc](cljc/my-directory/API.md)"
  ;   "* [my-directory/api.cljs](cljs/my-directory/API.md)"]
  ;
  ; @return (strings in vector)
  [options]
  (let [layers @read.state/LAYERS]
       (letfn [(f [links layer-name _]
                  (process-layer-links options layer-name links))]
              (reduce-kv f [] layers))))

(defn process-cover-subtitle
  ; @param (map) options
  ;  {:path (string)}
  ;
  ; @example
  ;  (process-cover-subtitle {...})
  ;  =>
  ;  "<p>Documentation of the <strong>my-repository</strong> Clojure / ClojureScript library</p>"
  ;
  ; @return (string)
  [{:keys [path]}]
  (let [clj-library?    (process.helpers/clj-library?)
        cljs-library?   (process.helpers/cljs-library?)
        repository-name (string/after-first-occurence path "/" {:return? true})]
       (str "<p>Documentation of the <strong>"repository-name"</strong>"
                            (if clj-library?  " Clojure ")
                            (if (and clj-library? cljs-library?) "/")
                            (if cljs-library? " ClojureScript ")
                            "library</p>")))

(defn process-cover-title
  ; @param (map) options
  ;  {:path (string)}
  ;
  ; @example
  ;  (process-subtitle {...})
  ;  =>
  ;  "# <strong>my-repository</strong>"
  ;
  ; @return (string)
  [{:keys [path]}]
  (let [repository-name (string/after-first-occurence path "/" {:return? true})]
       (str "# <strong>"repository-name"</strong>")))

(defn process-cover
  ; @param (map) options
  ;
  ; @return (map)
  [options]
  {"title"       (process-cover-title       options)
   "subtitle"    (process-cover-subtitle    options)
   "links"       (process-cover-links       options)
   "description" (process-cover-description options)})

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-cover!
  ; @param (map) options
  [options]
  (reset! process.state/COVER (process-cover options)))
