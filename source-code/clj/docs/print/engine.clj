
(ns docs.print.engine
    (:require [docs.import.state    :as import.state]
              [docs.print.helpers   :as print.helpers]
              [docs.process.helpers :as process.helpers]
              [docs.process.state   :as process.state]
              [io.api               :as io]
              [normalize.api        :as normalize]
              [string.api           :as string]
              [vector.api           :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-api-function-code
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ; @param (map) function-data
  ;
  ; @return (string)
  [_ _ _ function-data]
  (let [code (get function-data "code")]
       (str "\n\n<details>"
            "\n<summary>Source code</summary>"
            "\n\n```"
           ;"\n@code"
            "\n"code
            "\n```"
            "\n\n</details>")))

(defn print-api-function-require
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ; @param (map) function-data
  ; {}
  ;
  ; @return (string)
  [_ layer-name api-filepath function-data]
  (let [function-name  (get    function-data "name")
        params         (get-in function-data ["header" "params"])
        namespace (get-in @import.state/LAYERS [layer-name api-filepath "namespace"])
        api-filepath (string/replace-part api-filepath "_" "-")]
       (str "\n\n<details>"
            "\n<summary>Require</summary>"
            "\n\n```"
           ;"\n@require"
            "\n(ns my-namespace (:require ["namespace" :refer ["function-name"]]))"
            "\n\n("namespace"/"function-name
            (if-not (empty? params) " ...") ")"
            "\n("function-name
            (if-not (empty? params)
                    (let [tab (string/multiply " " (-> namespace count inc))]
                         (str tab " ...")))
            ")"
            "\n```"
            "\n\n</details>")))

(defn print-api-function-params
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ; @param (map) function-data
  ; {}
  ;
  ; @return (string)
  [_ _ _ function-data]
  (let [params (get-in function-data ["header" "params"])]
       (if (-> params empty? not)
           (str "\n\n```\n" (string/join params "\n") "\n```"))))

(defn print-api-function-usages
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ; @param (map) function-data
  ; {}
  ;
  ; @return (string)
  [_ _ _ function-data]
  (let [usages (get-in function-data ["header" "usages"])]
       (if (-> usages empty? not)
           (letfn [(f [usages usage]
                      (str usages "\n\n```"usage"```"))]
                  (reduce f "" usages)))))

(defn print-api-function-examples
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ; @param (map) function-data
  ; {}
  ;
  ; @return (string)
  [_ _ _ function-data]
  (let [examples (get-in function-data ["header" "examples"])]
       (if (-> examples empty? not)
           (letfn [(f [examples example]
                      (str examples "\n\n```"example"```"))]
                  (reduce f "" examples)))))

(defn print-api-function-return
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ; @param (map) function-data
  ; {}
  ;
  ; @return (string)
  [_ _ _ function-data]
  (if-let [return (get-in function-data ["header" "return"])]
          (str "\n\n```\n"return"\n```")))

(defn print-api-function-header
  ; @param (map) options
  ; {:print-options (keywords in vector)}
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ; @param (map) function-data
  ;
  ; @return (string)
  [{:keys [print-options] :as options} layer-name api-filepath function-data]
  (str (if (vector/contains-item? print-options :params)
           (print-api-function-params   options layer-name api-filepath function-data))
       (if (vector/contains-item? print-options :usages)
           (print-api-function-usages   options layer-name api-filepath function-data))
       (if (vector/contains-item? print-options :examples)
           (print-api-function-examples options layer-name api-filepath function-data))
       (if (vector/contains-item? print-options :return)
           (print-api-function-return   options layer-name api-filepath function-data))
       (if (vector/contains-item? print-options :code)
           (print-api-function-code options layer-name api-filepath function-data))
       (if (vector/contains-item? print-options :require)
           (print-api-function-require  options layer-name api-filepath function-data))))

(defn print-api-function
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ; @param (map) function-data
  ; {}
  ;
  ; @return (string)
  [options layer-name api-filepath function-data]
  (let [function-name (get function-data "name")]
       (str "\n\n### "function-name
            (print-api-function-header options layer-name api-filepath function-data))))

(defn print-api-functions
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ;
  ; @return (string)
  [options layer-name api-filepath]
  (let [functions (get-in @process.state/LAYERS [layer-name api-filepath "functions"])
        functions (print.helpers/sort-functions functions)]
       (letfn [(f [functions function-data]
                  (if functions (str functions "\n\n---" (print-api-function options layer-name api-filepath function-data))
                                (str functions  (print-api-function options layer-name api-filepath function-data))))]
              (reduce f nil functions))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-api-constants
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ;
  ; @return (string)
  [_ layer-name api-filepath])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-api-links
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ;
  ; @return (string)
  [_ layer-name api-filepath]
  (let [functions (get-in @process.state/LAYERS [layer-name api-filepath "functions"])
        functions (print.helpers/sort-functions functions)
        constants (get-in @process.state/LAYERS [layer-name api-filepath "constants"])
        constants (print.helpers/sort-constants functions)]
       (letfn [(f [links function-data] (let [function-name (get function-data "name")
                                              function-link (normalize/clean-text function-name)]
                                             (str links "\n\n- ["function-name"](#"function-link")")))]
              (str "\n\n### Index" (reduce f "" functions)))))

(defn print-api-breadcrumbs
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ;
  ; @return (string)
  [{:keys [abs-path]} layer-name api-filepath]
  (letfn [(f [r _] (str r "../"))]
         (let [api-namespace (get-in @import.state/LAYERS [layer-name api-filepath "namespace"])
               depth         (-> api-namespace (string/split #"\.")
                                               (count))
               steps         (reduce f nil (range 0 depth))]
              (str "\n\n##### [README](../"steps"README.md) > [DOCUMENTATION]("steps"COVER.md) > "api-namespace""))))

(defn print-api-title
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ;
  ; @return (string)
  [_ layer-name api-filepath]
  (let [api-namespace (get-in @import.state/LAYERS [layer-name api-filepath "namespace"])]
       (str "\n# "api-namespace (case layer-name "clj" " Clojure" "cljc" " isomorphic" "cljs" " ClojureScript")
            " namespace")))

(defn print-api-footer
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ;
  ; @return (string)
  [_ _ _]
  (let [footer (get @process.state/COMMON "footer")]
       (str "\n\n"footer)))

(defn print-api-file
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ;
  ; @return (string)
  [options layer-name api-filepath]
  (str (print-api-title       options layer-name api-filepath)
       (print-api-breadcrumbs options layer-name api-filepath)
       (print-api-links       options layer-name api-filepath)
       (print-api-constants   options layer-name api-filepath)
       (print-api-functions   options layer-name api-filepath)
       "\n\n---"
       (print-api-footer      options layer-name api-filepath)
       "\n"))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-api-file!
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  [options layer-name api-filepath]
  (let [md-path          (process.helpers/md-path options layer-name api-filepath)
        api-doc-filepath (str md-path "/API.md")
        api-doc          (print-api-file options layer-name api-filepath)]
       (io/write-file! api-doc-filepath api-doc {:create? true})))

(defn print-layer!
  ; @param (map) options
  ; @param (string) layer-name
  [options layer-name]
  (let [layer-data (get @process.state/LAYERS layer-name)]
       (letfn [(f [_ api-filepath _] (print-api-file! options layer-name api-filepath))]
              (reduce-kv f nil layer-data))))

(defn print-layers!
  ; @param (map) options
  [options]
  (letfn [(f [_ layer-name _] (print-layer! options layer-name))]
         (reduce-kv f nil @process.state/LAYERS)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-cover-breadcrumbs
  ; @param (map) options
  ;
  ; @return (string)
  [_]
  (str "\n\n##### [README](../README.md) > DOCUMENTATION"))

(defn print-cover-footer
  ; @param (map) options
  ;
  ; @return (string)
  [_]
  (let [footer (get @process.state/COMMON "footer")]
       (str "\n\n"footer)))

(defn print-cover-links
  ; @param (map) options
  ;
  ; @return (string)
  [_]
  (let [clj-links  (get-in @process.state/COVER ["links" "clj"])
        cljc-links (get-in @process.state/COVER ["links" "cljc"])
        cljs-links (get-in @process.state/COVER ["links" "cljs"])]
       (letfn [(f [links link]
                  (str links"\n"link))]
              (str (if (-> clj-links  empty? not) (reduce f "\n\n### Clojure namespaces\n"       clj-links))
                   (if (-> cljc-links empty? not) (reduce f "\n\n### Isomorphic namespaces\n"    cljc-links))
                   (if (-> cljs-links empty? not) (reduce f "\n\n### ClojureScript namespaces\n" cljs-links))))))

(defn print-cover-subtitle
  ; @param (map) options
  ;
  ; @return (string)
  [_]
  (let [subtitle (get @process.state/COMMON "subtitle")]
       (str "\n\n"subtitle)))

(defn print-cover-title
  ; @param (map) options
  ;
  ; @return (string)
  [_]
  (let [title (get @process.state/COVER "title")]
       (str "\n\n"title)))

(defn print-cover
  ; @param (map) options
  ;
  ; @return (string)
  [options]
  (str (print-cover-title       options)
       (print-cover-subtitle    options)
       (print-cover-breadcrumbs options)
       (print-cover-links       options)
       "\n\n---"
       (print-cover-footer      options)
       "\n"))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-cover!
  ; @param (map) options
  ; {:abs-path (string)
  ;  :output-dir (strs)}
  [{:keys [abs-path output-dir] :as options}]
  (let [cover-filepath (str abs-path"/"output-dir"/COVER.md")
        cover          (print-cover options)]
       (io/write-file! cover-filepath cover {:create? true})))
