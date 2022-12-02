
(ns docs.process.engine
    (:require [candy.api            :refer [return]]
              [docs.import.state    :as import.state]
              [docs.process.helpers :as process.helpers]
              [docs.process.state   :as process.state]
              [docs.read.state      :as read.state]
              [map.api              :as map]
              [string.api           :as string]
              [vector.api           :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-function-params
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ; @param (map) function-data
  ; {"header"
  ;   {"params" (map)(opt)
  ;     {"name" (string)
  ;     {"optional?" (boolean)
  ;     {"types" (string)}}}
  ;
  ; @example
  ; (process-function-params {...} "clj" "my-repository/source-code/api.clj" {...})
  ; =>
  ; ["@param (string)(opt) my-param"]
  ;
  ; @return (strings in vector)
  [_ _ _ function-data]
  (if-let [params (get-in function-data ["header" "params"])]
          (letfn [(f [params param]
                     (let [name      (get param "name")
                           optional? (get param "optional?")
                           types     (get param "types")
                           sample    (get param "sample")]
                          (conj params (str "@param ("types")"
                                            (if optional? "(opt)")
                                            " "name
                                            (if sample (str "\n"sample))))))]
                 (reduce f [] params))))

(defn process-function-usages
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ; @param (map) function-data
  ; {"header"
  ;   {"usages" (maps in vector)(opt)
  ;     [{"call" (string)}]}}
  ;
  ; @example
  ; (process-function-usages {...} "clj" "my-repository/source-code/api.clj" {...})
  ; =>
  ; ["\n@usage ..."]
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
  ; @param (string) api-filepath
  ; @param (map) function-data
  ; {"header"
  ;   {"examples" (maps in vector)(opt)
  ;     [{"call" (string)}]}}
  ;
  ; @example
  ; (process-function-examples {...} "clj" "my-repository/source-code/api.clj" {...})
  ; =>
  ; ["\n@example ..."]
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
  ; @param (string) api-filepath
  ; @param (map) function-data
  ; {"header"
  ;   {"return" (map)(opt)
  ;     {"sample" (string)(opt)
  ;      "types" (string)(opt)}}}
  ;
  ; @example
  ; (process-function-return {...} "clj" "my-repository/source-code/api.clj" {...})
  ; =>
  ; "@return(string)"
  ;
  ; @return (string)
  [_ _ _ function-data]
  (if-let [types (get-in function-data ["header" "return" "types"])]
          (if-let [sample (get-in function-data ["header" "return" "sample"])]
                  (str "@return ("types")"sample)
                  (str "@return ("types")"))))


(defn process-function-header
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ; @param (map) function-data
  ;
  ; @example
  ; (process-function-header {...} "clj" "my-repository/source-code/api.clj" {...})
  ; =>
  ; (?)
  ;
  ; @return (map)
  ; {"examples" (strings in vector)
  ;  "params" (strings in vector)
  ;  "return" (string)
  ;  "usages" (strings in vector)}
  [options layer-name api-filepath function-data]
  {"examples" (process-function-examples options layer-name api-filepath function-data)
   "params"   (process-function-params   options layer-name api-filepath function-data)
   "return"   (process-function-return   options layer-name api-filepath function-data)
   "usages"   (process-function-usages   options layer-name api-filepath function-data)})

(defn process-function
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ; @param (map) function-data
  ;
  ; @example
  ; (process-function {...} "clj" "my-repository/source-code/api.clj" {...})
  ; =>
  ; (?)
  ;
  ; @return (map)
  ; {"header" (map)
  ;  "name" (string)}
  [options layer-name api-filepath function-data]
  {"header" (process-function-header options layer-name api-filepath function-data)
   "code"   (get function-data "code")
   "name"   (get function-data "name")})

(defn process-functions
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ;
  ; @example
  ; (process-functions {...} "clj" "my-repository/source-code/api.clj")
  ; =>
  ; {}
  ;
  ; @return (map)
  [options layer-name api-filepath]
  (let [functions (get-in @read.state/LAYERS [layer-name api-filepath "functions"])]
       (letfn [(f [result function-data]
                  (conj result (process-function options layer-name api-filepath function-data)))]
              (reduce f [] functions))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-api-file
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ;
  ; @example
  ; (process-api-file {...} "clj" "my-repository/source-code/api.clj")
  ; =>
  ; {}
  ;
  ; @return (map)
  ; {}
  [options layer-name api-filepath]
  {"functions" (process-functions options layer-name api-filepath)})
  ;"constants" (process-constants options layer-name api-filepath)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-layer
  ; @param (map) options
  ; @param (string) layer-name
  ;
  ; @example
  ; (process-layer {...} "clj")
  ; =>
  ; {"my-repository/source-code/api.clj" {}}
  ;
  ; @return (map)
  [options layer-name]
  (let [layer-data (get @read.state/LAYERS layer-name)]
       (letfn [(f [layer-data api-filepath api-data]
                  (assoc layer-data api-filepath (process-api-file options layer-name api-filepath)))]
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
  ; (process-cover-description {...})
  ; =>
  ; "..."
  ;
  ; @return (string)
  [_]
  (str "<p>This documentation is generated by the <strong>docs-api</strong> engine</p>\n"
       "https://github.com/bithandshake/docs-api"))

(defn process-layer-links
  ; @param (map) options
  ;  {:abs-path (string)
  ;   :output-dir (string)}
  ; @param (string) layer-name
  ; @param (strings in vector) links
  ;
  ; @return (strings in vector)
  [{:keys [abs-path output-dir] :as options} layer-name links]
  (let [layer-data (get @read.state/LAYERS layer-name)
        api-files  (map/get-keys layer-data)]
       ; Az f0 függvény vizsgálja, hogy az api fájlban van-e bármilyen átirányitás
       ; ha nincs akkor nem készül hozzá API.md fájl ezért a linket sem szükséges betenni a COVER.md fájlba
       (letfn [(f0 [api-filepath] (or (vector/nonempty? (get-in @read.state/LAYERS [layer-name api-filepath "functions"]))
                                      (vector/nonempty? (get-in @read.state/LAYERS [layer-name api-filepath "constants"]))))
               (f [links api-filepath]
                  (let [api-namespace (get-in @import.state/LAYERS [layer-name api-filepath "namespace"])
                        md-path   (process.helpers/md-path options layer-name api-filepath)
                        rel-path  (-> md-path (string/not-starts-with! abs-path)
                                              (string/not-starts-with! "/")
                                              (string/not-starts-with! output-dir)
                                              (string/not-starts-with! "/"))]
                       (if (f0 api-filepath)
                           (conj   links (str "* ["api-namespace"]("rel-path"/API.md) ["layer-name"]"))
                           (return links))))]
              (reduce f links (vector/abc-items api-files)))))

(defn process-cover-links
  ; @param (map) options
  ;
  ; @return (strings in vector)
  [options]
  (let [layers @read.state/LAYERS]
       (letfn [(f [links layer-name _]
                  (process-layer-links options layer-name links))]
              (reduce-kv f [] layers))))

(defn process-cover-subtitle
  ; @param (map) options
  ; {:lib-name (string)}
  ;
  ; @example
  ; (process-cover-subtitle {...})
  ; =>
  ; "<p>Documentation of the <strong>my-repository</strong> Clojure / ClojureScript library</p>"
  ;
  ; @return (string)
  [{:keys [lib-name]}]
  (let [clj-library?  (process.helpers/clj-library?)
        cljs-library? (process.helpers/cljs-library?)]
       (str "<p>Documentation of the <strong>"lib-name"</strong>"
            (if clj-library?  " Clojure ")
            (if (and clj-library? cljs-library?) "/")
            (if cljs-library? " ClojureScript ")
            "library</p>")))

(defn process-cover-title
  ; @param (map) options
  ; {:lib-name (string)}
  ;
  ; @example
  ; (process-subtitle {...})
  ; =>
  ; "# <strong>my-repository</strong>"
  ;
  ; @return (string)
  [{:keys [lib-name]}]
  (str "# <strong>"lib-name"</strong>"))

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
