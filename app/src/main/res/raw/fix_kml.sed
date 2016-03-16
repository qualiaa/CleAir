s/SimpleData/Data/g
s=\(<Data[^<]*>\)\(.*\)\(</Data>\)=\1<value>\2</value>\3=g
s=\(<fill>\)0\(</fill>\)=\11\2=g
/<Placemark/a\
    <styleUrl>#PolyStyle00</styleUrl>
/<Style>/d
s=</\?SchemaData[^>]*>==g

\#</Document>#i\
  <Style id="PolyStyle00">\
    <LabelStyle>\
      <color>00000000</color>\
      <scale>0.000000</scale>\
    </LabelStyle>\
    <LineStyle>\
      <color>ff6e6e6e</color>\
      <width>0.400000</width>\
    </LineStyle>\
    <PolyStyle>\
      <color>fff7cdee</color>\
      <outline>1</outline>\
    </PolyStyle>\
  </Style>
