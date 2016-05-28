# spa2run

`spa2run` is a command line tool written in Java that takes a structured pushdown automaton (SPA) spec written in the YAML format and provides a shell session for querying and instrumentation. This tool is released under the GNU Public License version 3.0 and makes use of the `AA4J` library available [here]()https://github.com/cereda/aa:

> Cereda, Paulo Roberto Massa; José Neto, João. AA4J: uma biblioteca para implementação de autômatos adaptativos. Em: Memórias do X Workshop de Tecnologia Adaptativa - WTA 2016. EPUSP, São Paulo. ISBN: 978-85-86686-86-3, pp. 16-26. 28 e 29 de Janeiro, 2016.

Apache Maven is required to build `spa2run` from sources. Run:

```bash
$ mvn assembly:assembly
```

Sample execution:

```bash
[paulo@cambridge spa2run] $ java -jar spa2run.jar 
               ___               
 ____ __  __ _|_  )_ _ _  _ _ _  
(_-< '_ \/ _` |/ /| '_| || | ' \ 
/__/ .__/\__,_/___|_|  \_,_|_||_|
   |_|   

----------------------------------------------------------------------
                       AN EXCEPTION WAS THROWN                        
----------------------------------------------------------------------
The tool requires at least one automaton spec. Make sure to provide
such file and try again.
----------------------------------------------------------------------
```
