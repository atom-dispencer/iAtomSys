#
# Build with Mix/Escript
#
echo ""
echo " >> Mixing assembler..."
mix escript.build

#
# Invoke executable
#
echo ""
echo " >> Starting assembler (escript)..."
escript ./iatomsys_assembler
