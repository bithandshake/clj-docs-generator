
(ns docs.core.patterns)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @constant (map)
(def OPTIONS-PATTERN
     {:abs-path          {:f*   string?
                          :not* empty?
                          :e*   "key :abs-path must be a nonempty string!"}
      :author            {:opt* true
                          :f*   string?
                          :not* empty?
                          :e*   "key :author must be a nonempty string!"}
      :code-dirs         {:and* [vector? #(every? string? %)]
                          :not* empty?
                          :e*   "key :code-dirs must be a nonempty vector with string items!"}
      :lib-name          {:f*   string?
                          :not* empty?
                          :e*   "key :lib-name must be a nonempty string!"}
      :output-dir        {:f*   string?
                          :not* empty?
                          :e*   "key :output-dir must be a nonempty string!"}
      :print-options     {:opt* true
                          :and* [vector? #(every? keyword? %)]
                          :not* empty?
                          :e*   "key :print-options must be a nonempty vector with keyword items!"}
      :public-namespaces {:opt* true
                          :and* [vector?]
                          :e*   "key :public-namespaces must be a vector with regex pattern or string items!"}
      :website           {:opt* true
                          :f*   string?
                          :not* empty?
                          :e*   "key :website must be a nonempty string!"}})
