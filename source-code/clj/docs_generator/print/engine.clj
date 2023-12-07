
(ns docs-generator.print.engine
    (:require [docs-generator.import.state  :as import.state]
              [docs-generator.print.utils   :as print.utils]
              [docs-generator.process.state :as process.state]
              [docs-generator.process.utils :as process.utils]
              [fruits.normalize.api         :as normalize]
              [fruits.string.api            :as string]
              [fruits.vector.api            :as vector]
              [io.api                       :as io]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-api-function-description
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ; @param (map) function-data
  ;
  ; @return (string)
  [_ _ _ function-data]
  (if-let [description (get-in function-data ["header" "description"])]
          (str "\n\n```\n" description"```")))

(defn print-api-function-warning
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ; @param (map) function-data
  ;
  ; @return (string)
  [_ _ _ function-data]
  (if-let [warning (get-in function-data ["header" "warning"])]
          (str "\n\n```\n" warning"```")))

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
        namespace      (get-in @import.state/LAYERS [layer-name api-filepath "namespace"])
        api-filepath   (string/replace-part api-filepath "_" "-")]
       (str "\n\n<details>"
            "\n<summary>Require</summary>"
            "\n\n```"
           ;"\n@require"
            "\n(ns my-namespace (:require ["namespace" :refer ["function-name"]]))"
            "\n\n("namespace"/"function-name
            (if-not (empty? params) " ...") ")"
            "\n("function-name
            (if-not (empty? params)
                    (let [tab (string/repeat " " (-> namespace count inc))]
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
           (letfn [(f0 [usages usage]
                       (str usages "\n\n```"usage"```"))]
                  (reduce f0 "" usages)))))

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
           (letfn [(f0 [examples example]
                       (str examples "\n\n```"example"```"))]
                  (reduce f0 "" examples)))))

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
  (str (if (vector/contains-item? print-options :warning)
           (print-api-function-warning     options layer-name api-filepath function-data))
       (if (vector/contains-item? print-options :description)
           (print-api-function-description options layer-name api-filepath function-data))
       (if (vector/contains-item? print-options :params)
           (print-api-function-params      options layer-name api-filepath function-data))
       (if (vector/contains-item? print-options :usages)
           (print-api-function-usages      options layer-name api-filepath function-data))
       (if (vector/contains-item? print-options :examples)
           (print-api-function-examples    options layer-name api-filepath function-data))
       (if (vector/contains-item? print-options :return)
           (print-api-function-return      options layer-name api-filepath function-data))
       (if (vector/contains-item? print-options :code)
           (print-api-function-code        options layer-name api-filepath function-data))
       (if (vector/contains-item? print-options :require)
           (print-api-function-require     options layer-name api-filepath function-data))))

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
        functions (print.utils/sort-functions functions)]
       (letfn [(f0 [functions function-data]
                   (if functions (str functions "\n\n---" (print-api-function options layer-name api-filepath function-data))
                                 (str functions           (print-api-function options layer-name api-filepath function-data))))]
              (reduce f0 nil functions))))

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
        functions (print.utils/sort-functions functions)
        constants (get-in @process.state/LAYERS [layer-name api-filepath "constants"])
        constants (print.utils/sort-constants functions)]
       (letfn [(f0 [links function-data] (let [function-name (get function-data "name")
                                               function-link (normalize/clean-text function-name)]
                                              (str links "\n\n- ["function-name"](#"function-link")")))]
              (str "\n\n### Index" (reduce f0 "" functions)
                   "\n\n---"))))

(defn print-api-breadcrumbs
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ;
  ; @return (string)
  [_ layer-name api-filepath]
  (letfn [(f0 [r _] (str r "../"))]
         (let [api-namespace (get-in @import.state/LAYERS [layer-name api-filepath "namespace"])
               depth         (-> api-namespace (string/split #"\.")
                                               (count))
               steps         (reduce f0 nil (range 0 depth))]
              (str "\n\n##### [README](../"steps"README.md) > [DOCUMENTATION]("steps"COVER.md) > "api-namespace""))))

(defn print-api-title
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ;
  ; @return (string)
  [_ layer-name api-filepath]
  (let [api-namespace (get-in @import.state/LAYERS [layer-name api-filepath "namespace"])]
       (str "\n### "api-namespace
            "\n\nFunctional documentation of the "api-namespace" " ; <- Make this clickable + provide the available of source-code files by clicking on their names from anywhere where they are in the documentation.
            (case layer-name "clj" "Clojure" "cljc" "isomorphic" "cljs" "ClojureScript") " namespace"
            "\n\n---")))

(defn print-api-footer
  ; @param (map) options
  ; {:print-options (keywords in vector)}
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ;
  ; @return (string)
  [{:keys [print-options]} _ _]
  (if (vector/contains-item? print-options :credits)
      (let [credits (get @process.state/COMMON "credits")]
           (str "\n\n---\n\n"credits"\n"))))

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
       (print-api-footer      options layer-name api-filepath)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-api-file!
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  [options layer-name api-filepath]
  (let [md-path          (process.utils/md-path options layer-name api-filepath)
        api-doc-filepath (str md-path "/API.md")
        api-doc          (print-api-file options layer-name api-filepath)]
       (io/write-file! api-doc-filepath api-doc {:create? true})))

(defn print-layer!
  ; @param (map) options
  ; @param (string) layer-name
  [options layer-name]
  (let [layer-data (get @process.state/LAYERS layer-name)]
       (letfn [(f0 [_ api-filepath _] (print-api-file! options layer-name api-filepath))]
              (reduce-kv f0 nil layer-data))))

(defn print-layers!
  ; @param (map) options
  [options]
  (letfn [(f0 [_ layer-name _] (print-layer! options layer-name))]
         (reduce-kv f0 nil @process.state/LAYERS)))

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
  ; {:print-options (keywords in vector)}
  ;
  ; @return (string)
  [{:keys [print-options]}]
  (if (vector/contains-item? print-options :credits)
      (let [credits (get @process.state/COMMON "credits")]
           (str "\n\n---\n\n"credits"\n"))))

(defn print-cover-links
  ; @param (map) options
  ;
  ; @return (string)
  [_]
  (let [clj-links  (get-in @process.state/COVER ["links" "clj"])
        cljc-links (get-in @process.state/COVER ["links" "cljc"])
        cljs-links (get-in @process.state/COVER ["links" "cljs"])]
       (letfn [(f0 [links link] (str links"\n"link))]
              (str (if (-> clj-links  empty? not) (reduce f0 "\n\n### Clojure namespaces\n"       clj-links))
                   (if (-> cljc-links empty? not) (reduce f0 "\n\n### Isomorphic namespaces\n"    cljc-links))
                   (if (-> cljs-links empty? not) (reduce f0 "\n\n### ClojureScript namespaces\n" cljs-links))))))

(defn print-cover-subtitle
  ; @param (map) options
  ;
  ; @return (string)
  [_]
  (let [subtitle (get @process.state/COMMON "subtitle")]
       (str "\n\n"subtitle"\n\n---\n\n")))

(defn print-cover-title
  ; @param (map) options
  ;
  ; @return (string)
  [_]
  (let [title (get @process.state/COVER "title")]
       (str "\n"title)))

(defn print-cover
  ; @param (map) options
  ;
  ; @return (string)
  [options]
  (str (print-cover-title       options)
       (print-cover-subtitle    options)
       (print-cover-breadcrumbs options)
       (print-cover-links       options)
       (print-cover-footer      options)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-cover!
  ; @param (map) options
  ; {:output-dir (strs)}
  [{:keys [output-dir] :as options}]
  (let [cover-filepath (str output-dir"/COVER.md")
        cover          (print-cover options)]
       (io/write-file! cover-filepath cover {:create? true})))
