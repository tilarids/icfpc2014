
N=$1

> array_$N.gcc

for i in `seq 0 $(($N-1))`; do
cat >> array_$N.gcc <<EOF
    LDC 0
EOF
done

cat >> array_$N.gcc <<EOF

    LDF \$create_accessor_${N}\$
    AP $N
    RTN

\$create_accessor_$N\$:
    LDF  \$accessor$N\$
    RTN

\$accessor${N}\$:

    LD  0   0       ; opcode
    LDC 100         ; read?
    CEQ
    TSEL    \$\$pc\$+1\$ \$accessor${N}_w\$

EOF

for i in `seq 0 $(($N-1))`; do
cat >> array_$N.gcc <<EOF

    LD  0   1       ; ix
    LDC $i
    CEQ
    TSEL    \$\$read${i}\$+2\$ \$\$pc\$+1\$
EOF
done

cat >> array_$N.gcc <<EOF
    LDC  0           ; nothing found
    RTN

\$accessor${N}_w\$:
EOF

for i in `seq 0 $(($N-1))`; do
cat >> array_$N.gcc <<EOF
    LD  0   1       ; ix
    LDC $i
    CEQ
    TSEL    \$\$write${i}\$+4\$ \$\$pc\$+1\$
EOF
done

cat >> array_$N.gcc <<EOF
    LDC  0           ; nothing found
    RTN
EOF

for i in `seq 0 $(($N-1))`; do
cat >> array_$N.gcc <<EOF

\$read${i}\$:
    LD  2   $i
    RTN
; read accessor
    LDF \$read${i}\$
    RTN

\$write${i}\$:
    LD  2   $i
    LD  0   0
    ST  2   $i
    RTN
; write accessor
    LDF \$write${i}\$
    RTN

EOF
done
