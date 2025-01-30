" Vim syntax file for chai code

" Quit when a (custom) syntax file was already loaded
if exists("b:current_syntax")
  finish
endif

" keywords
syn keyword chaiKeyword if elif else while for in def lambda return and or not var let type of match case 
syn keyword chaiKeyword assert break continue pass class implement trait self

" types
syn keyword chaiType Int Float String Bool True False Void

" comments and TODOs
syn keyword chaiTodo contained TODO FIXME
syn match chaiComment "#.*$" contains=chaiTodo

" values
syn match chaiValue '\<\d\+'
syn match chaiValue '[-+]\d\+'

" floating point value regexes taken from c.vim syntax file
syn match chaiValue '\<\d\+\.\d*\(e[-+]\=\d\+\)\=[fl]\='
syn match chaiValue '\<\.\d\+\(e[-+]\=\d\+\)\=[fl]\=\>'
syn match chaiValue '\<\d\+e[-+]\=\d\+[fl]\=\>'
syn region chaiValue start='"' end='"' skip='\\"'

" hook it up
hi def link chaiKeyword Keyword
hi def link chaiType Type
hi def link chaiValue Constant
hi def link chaiComment Comment
hi def link chaiTodo Todo
hi def link chaiError Error

