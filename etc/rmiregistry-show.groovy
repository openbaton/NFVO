import java.rmi.ConnectException
import java.rmi.RemoteException
import java.rmi.registry.LocateRegistry
import java.rmi.registry.Registry

if (args.length < 2)
{
   println "A host name (String) and a port (Integer) must be provided."
   println "\tExample: groovy rmiPortNamesDisplay localhost 1099"
   System.exit(-2)
}

host = args[0]
port = 1099
try
{
   port = Integer.valueOf(args[1])
}
catch (NumberFormatException numericFormatEx)
{
   println "The provided port value '${args[1]}' is not an integer."
   System.exit(-1)
}

registry = LocateRegistry.getRegistry(host, port)
boundNames = registry.list()
println "Names bound to RMI registry at host ${host} and port ${port}:"
boundNames.each{println "\t${it}"}
