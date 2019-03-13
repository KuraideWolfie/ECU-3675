# ECU-3675

## Assignment 03
### Assignment Description
This assignment, using Cinnameg, was abut implementing the RSA cryptosystem. It was comprised of four modules - convert, decipher, encipher, and keygen, and provided an emphasis on higher-order functional programming, as well as using folding in an assignment. A procedural function is also demonstrated in convert.
### Source Files
convert.cmg, keygen.cmg, encipher.cmg, decipher.cmg
### Compilation, Testing, and Known Issues
The following testing command, using diff, is meant for testing if the original plaintext is the same after encryption/decryption.
```
Compile:
cmgc -l convert
cmgc -l keygen
cmgc -l encipher
cmgc -l decipher

Testing:
cmgr -t -g+ -s1024 keygen < "./keygen.in"
cmgr -t -g+ -s1024 encipher ./Test/test.txt ./Test/test.cph
cmgr -t -g+ -s2048 decipher ./Test/test.cph ./Test/test.pxt
diff ./Test/test.txt ./Test/test.pxt
```