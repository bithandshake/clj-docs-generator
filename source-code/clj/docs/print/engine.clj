
(ns docs.print.engine
    (:require [docs.print.helpers :as print.helpers]
              [docs.process.state :as process.state]
              [io.api             :as io]
              [string.api         :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-api-function-code
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
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
  ; @param (string) directory-name
  ; @param (map) function-data
  ;  {}
  ;
  ; @return (string)
  [_ _ directory-name function-data]
  (let [function-name  (get    function-data "name")
        params         (get-in function-data ["header" "params"])
        directory-name (string/replace-part directory-name "_" "-")]
       (str "\n\n<details>"
            "\n<summary>Require</summary>"
            "\n\n```"
           ;"\n@require"
            "\n(ns my-namespace (:require ["directory-name".api :as "directory-name" :refer ["function-name"]]))"
            "\n\n("directory-name"/"function-name
            (if-not (empty? params) " ...") ")"
            "\n("function-name
            (if-not (empty? params)
                    (let [tab (string/multiply " " (-> directory-name count inc))]
                         (str tab " ...")))
            ")"
            "\n```"
            "\n\n</details>")))

(defn print-api-function-params
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ; @param (map) function-data
  ;  {}
  ;
  ; @return (string)
  [_ _ _ function-data]
  (let [params (get-in function-data ["header" "params"])]
       (if (-> params empty? not)
           (str "\n\n```\n" (string/join params "\n") "\n```"))))

(defn print-api-function-usages
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ; @param (map) function-data
  ;  {}
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
  ; @param (string) directory-name
  ; @param (map) function-data
  ;  {}
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
  ; @param (string) directory-name
  ; @param (map) function-data
  ;  {}
  ;
  ; @return (string)
  [_ _ _ function-data]
  (if-let [return (get-in function-data ["header" "return"])]
          (str "\n\n```\n"return"\n```")))

(defn print-api-function-header
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ; @param (map) function-data
  ;
  ; @return (string)
  [options layer-name directory-name function-data]
  (str (print-api-function-params   options layer-name directory-name function-data)
       (print-api-function-usages   options layer-name directory-name function-data)
       (print-api-function-examples options layer-name directory-name function-data)
       (print-api-function-return   options layer-name directory-name function-data)
       (print-api-function-code     options layer-name directory-name function-data)
       (print-api-function-require  options layer-name directory-name function-data)))

(defn print-api-function
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ; @param (map) function-data
  ;  {}
  ;
  ; @return (string)
  [options layer-name directory-name function-data]
  (let [function-name (get function-data "name")]
       (str "\n\n### "function-name
           ;"\n###### "layer-name"/"directory-name"/api."layer-name
            (print-api-function-header options layer-name directory-name function-data))))

(defn print-api-functions
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ;
  ; @return (string)
  [options layer-name directory-name]
  (let [functions (get-in @process.state/LAYERS [layer-name directory-name "functions"])
        functions (print.helpers/sort-functions functions)]
       (letfn [(f [functions function-data]
                  (if functions (str functions "\n\n---" (print-api-function options layer-name directory-name function-data))
                                (str functions "\n\n"    (print-api-function options layer-name directory-name function-data))))]
              (reduce f nil functions))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-api-constants
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ;
  ; @return (string)
  [_ layer-name directory-name])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-api-breadcrumbs
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ;
  ; @return (string)
  [_ layer-name directory-name]
  (let [directory-name (string/replace-part directory-name "_" "-")]
       (str "\n\n<strong>[README](../../../README.md) > [DOCUMENTATION](../../COVER.md) > "directory-name".api</strong>")))

(defn print-api-subtitle
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ;
  ; @return (string)
  [_ layer-name directory-name]
  (str "\n<p>Documentation of the <strong>"directory-name"/api."layer-name"</strong> file</p>"))

(defn print-api-title
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ;
  ; @return (string)
  [_ layer-name directory-name]
  (let [directory-name (string/replace-part directory-name "_" "-")]
       (str "\n# <strong>"directory-name".api</strong> namespace")))

(defn print-api
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ;
  ; @return (string)
  [options layer-name directory-name]
  (str (print-api-title       options layer-name directory-name)
       (print-api-subtitle    options layer-name directory-name)
       (print-api-breadcrumbs options layer-name directory-name)
       (print-api-constants   options layer-name directory-name)
       (print-api-functions   options layer-name directory-name)
       "\n"))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-directory!
  ; @param (map) options
  ;  {:path (string)}
  ; @param (string) layer-name
  ; @param (string) directory-name
  [{:keys [path] :as options} layer-name directory-name]
  (let [api-doc-filepath (str path"/documentation/"layer-name"/"directory-name"/API.md")
        api-doc          (print-api options layer-name directory-name)]
       (io/write-file! api-doc-filepath api-doc {:create? true})))

(defn print-layer!
  ; @param (map) options
  ; @param (string) layer-name
  [options layer-name]
  (let [layer-data (get @process.state/LAYERS layer-name)]
       (letfn [(f [_ directory-name _] (print-directory! options layer-name directory-name))]
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
  (str "\n\n<strong>[README](../README.md) > DOCUMENTATION</strong>"))

(defn print-cover-description
  ; @param (map) options
  ;
  ; @return (string)
  [_]
  (let [description (get @process.state/COVER "description")]
       (str "\n\n---\n\n"description)))

(defn print-cover-links
  ; @param (map) options
  ;
  ; @return (string)
  [_]
  (let [links (get @process.state/COVER "links")]
       (letfn [(f [links link]
                  (str links"\n"link))]
              (reduce f "\n\n### Public namespaces" links))))

(defn print-cover-subtitle
  ; @param (map) options
  ;
  ; @return (string)
  [_]
  (let [subtitle (get @process.state/COVER "subtitle")]
       (str "\n"subtitle)))

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
       (print-cover-description options)
       "\n"))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-cover!
  ; @param (map) options
  ;  {:path (string)}
  [{:keys [path] :as options}]
  (let [cover-filepath (str path"/documentation/COVER.md")
        cover          (print-cover options)]
       (io/write-file! cover-filepath cover {:create? true})))
