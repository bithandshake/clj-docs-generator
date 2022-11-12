
(ns docs.engine
    (:require [candy.api         :refer [return]]
              [docs.helpers      :as helpers]
              [docs.state        :as state]
              [mid-fruits.string :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-def
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ; @param (string) name
  ; @param (string) value
  [options layer-name directory-name name value]
  (if-let [alias (string/before-first-occurence value "/" {:return? false})]
          (let [code-filepath (helpers/code-filepath options layer-name directory-name alias)]
               code-filepath)
          (let [alias (get-in @state/LAYERS [layer-name directory-name "refers" value])
                code-filepath (helpers/code-filepath options layer-name directory-name alias)]
               code-filepath)))

(defn process-defs
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  [options layer-name directory-name]
  (let [defs (get-in @state/LAYERS [layer-name directory-name "defs"])]
       (letfn [(f [result name value]
                  (assoc result name (process-def options layer-name directory-name name value)))]
              (reduce-kv f {} defs))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-directory
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) directory-name
  ; @param (map) directory-data
  ;
  ; @example
  ;  (process-directory {:path "my-submodules/my-repository"} "clj" "my-directory" {"aliases" {...}
  ;                                                                                 "defs"    {...}
  ;                                                                                 "refers"  {...}})
  ;  =>
  ;  {"aliases"   {...}
  ;   "constants" {}
  ;   "defs"      {...}
  ;   "refers"    {...}
  ;   "functions" {}}
  ;
  ; @return (map)
  ;  {"aliases" (map)
  ;   "constants" (map)
  ;   "defs" (map)
  ;   "functions" (map)
  ;   "refers" (map)}
  [options layer-name directory-name directory-data]
  (assoc directory-data "x" (process-defs options layer-name directory-name)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-layer
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (map) layer-data
  ;
  ; @example
  ;  (process-layer {:path "my-submodules/my-repository"} "clj" {"my-directory" {"aliases" {...}
  ;                                                                              "defs"    {...}
  ;                                                                              "refers"  {...}})
  ;  =>
  ;  {"my-directory" {}}
  ;
  ; @return (map)
  [options layer-name layer-data]
  (letfn [(f [layer-data directory-name directory-data]
             (assoc layer-data directory-name (process-directory options layer-name directory-name directory-data)))]
         (reduce-kv f {} layer-data)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn process-layers!
  ; @param (map) options
  [options]
  (let [layers @state/LAYERS]
       (letfn [(f [_ layer-name layer-data]
                  (let [layer-data (process-layer options layer-name layer-data)]
                       (swap! state/LAYERS assoc layer-name layer-data)))]
              (reduce-kv f nil layers))))
