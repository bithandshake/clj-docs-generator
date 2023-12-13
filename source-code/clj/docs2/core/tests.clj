
(ns docs2.core.tests)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @description
; https://github.com/bithandshake/cljc-validator
;
; @constant (map)
(def OPTIONS-TEST
     {:author        {:opt* true
                      :f*   string?
                      :not* empty?
                      :e*   ":author must be a nonempty string!"}
      :code-dirs     {:and* [vector? #(every? string? %)]
                      :not* empty?
                      :e*   ":code-dirs must be a nonempty vector with string items!"}
      :lib-name      {:f*   string?
                      :not* empty?
                      :e*   ":lib-name must be a nonempty string!"}
      :output-dir    {:f*   string?
                      :not* empty?
                      :e*   ":output-dir must be a nonempty string!"}
      :print-options {:opt* true
                      :and* [vector? #(every? keyword? %)]
                      :not* empty?
                      :e*   ":print-options must be a nonempty vector with keyword items!"}
      :website       {:opt* true
                      :f*   string?
                      :not* empty?
                      :e*   ":website must be a nonempty string!"}})
