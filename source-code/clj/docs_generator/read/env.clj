
(ns docs-generator.read.env
    (:require [docs-generator.detect.env   :as detect.env]
              [docs-generator.import.state :as import.state]
              [fruits.regex.api            :as regex]
              [fruits.string.api           :as string]
              [io.api                      :as io]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn alter-filepath
  ; @ignore
  ;
  ; @param (map) options
  ; {}
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ; @param (string) alias
  ;
  ; @return (string)
  [{:keys [code-dirs] :as options} layer-name api-filepath alias]
  ; If the reader cannot find a namespace in the same code-dir with the api file,
  ; it tries to detect it as a .cljc file and looks up every code-dir to find.
  (letfn [(f0 [[namespace %]] (if (= % alias) namespace))
          (f1 [code-dir namespace] (str code-dir "/" (-> namespace (string/replace-part "." "/")
                                                                   (string/replace-part "-" "_"))
                                                     ".cljc"))]
         (let [namespace (some f0 (get-in @import.state/LAYERS [layer-name api-filepath "aliases"]))]
              (letfn [(f3 [code-dir] (let [alter-filepath (f1 code-dir namespace)]
                                          (if (-> alter-filepath io/file-exists?)
                                              (-> alter-filepath))))]
                     (some f3 code-dirs)))))

(defn code-filepath
  ; @ignore
  ;
  ; @param (map) options
  ; @param (string) layer-name
  ; @param (string) api-filepath
  ; @param (string) alias
  ;
  ; @usage
  ; (code-filepath {...} "clj" "my-repository/source-code/my_directory/api.clj" "my-directory.my-file")
  ; =>
  ; "my-repository/source-code/my_directory/my_file.clj"
  ;
  ; @return (string)
  [options layer-name api-filepath alias]
  (letfn [(f0 [[namespace %]] (if (= % alias) namespace))]
         (let [code-dir  (detect.env/code-dir options layer-name api-filepath alias)
               namespace (some f0 (get-in @import.state/LAYERS [layer-name api-filepath "aliases"]))]
              (str code-dir "/" (-> namespace (string/replace-part "." "/")
                                              (string/replace-part "-" "_"))
                            "." (name layer-name)))))
