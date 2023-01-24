
(ns docs2.import.config)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @constant (regex pattern)
; Matches a constant if it's ...
; ... placed right after a newline character (drops out the matches from comments)
; ... starts with "(def "
; ... the name might contains LC or UC letters, and special chars: \- \_ \? \! \< \>
; ... the value starts! with a special char: \( \{ \[ \: \" (drops out the redirects)
(def CONSTANT-PATTERN #"\n\(def[ ]{1,}[a-zA-Z\d\-\_\?\!\<\>]{1,}[ ]{1,}[\(\{\[\:\#\"].{1,}")

; @ignore
;
; @constant (regex pattern)
(def FUNCTION-PATTERN #"\n\(defn[\- ]{1,}[a-zA-Z\d\-\_\?\!\<\>]{1,}[ ]{1,}")

; @ignore
;
; @constant (regex pattern)
; Matches a redirect if it's ...
; ... placed right after a newline character (drops out the matches from comments)
; ... starts with "(def "
; ... the name might contains LC or UC letters, and special chars: \- \_ \? \! \< \>
; ... the value starts! with a LC or UC letter or a digit (drops out the matches of static values)
(def REDIRECT-PATTERN #"\n\(def[ ]{1,}[a-zA-Z\d\-\_\?\!\<\>]{1,}[ ]{1,}[a-zA-Z\d].{1,}")



; WARNING! DEPRECATED
; @ignore
;
; @constant (regex pattern)
; Matches an ignore mark if it's ...
; ... placed right after a newline character and optinal white spaces
; ... commented and no other characters placed between the mark and the comment
;     sign than white chars
(def IGNORE-MARK-PATTERN #"\n[ ]{0,};[ ]{0,}@ignore")
; WARNING! DEPRECATED
