# Notes on Acme Paths

To make navigating the model a little easier, a path-based set constructor has been introduced. This can be used 
wherever set literals, select, collect, or set references could be used. The path constructor starts with a `/` 
followed by the root element. Subsequent path elements start with a `/` and contain a set reference, an optional
type discriminator, and an optional filter. These subsequent paths can be concatenated together to go further into the tree. 

For general structure for a path-based set constructor is:

`/root/set-reference1<filter>/set-reference2<filter>/…`

Where `root` can be any element, set reference has to follow the these rules:


| Root Element | Reference |
|--------------|-----------|
| System or set{system} | components, connectors, groups, properties, attachments |
| Component or set{Component} | ports, properties |
| Connector or set{Connector} | roles, properties |
| Port or set{Port} | attachedRoles, properties |
| Role or set{Role | attachedPorts, properties |

For example:

- `/system/components` will return the set of all components in the system “system”.
- `/system/components/ports` will return all the ports in all the components of “system”
- `/system/components/ports/attachedRoles` Will return all the roles that are attached to all the ports in all the components of the system.

It is possible to start at any level of the diagram:

- `/c/ports` will return all the ports in a component called `c`.

Filters are a way of reducing the set returned by the path constructor. For example, imagine the following types:

```
Port type PT = {
  Property i : float;
}

Component type CT = {
  Port p : PT;
  Property load : int;
}
```
It is possible to get only the `CT` components by specifying:

`/s/components:CT` or `/s/components:!CT` (Where `:` means components satisfying CT and `:!` means components declaring CT. Note, normally we use `:!`)

Alternatively, `/s/components:CT/ports:PT` gets all ports of type PT of all components of type CT.

It is also possible to use an expression to further filter the sets, e.g., `/s/components:CT[load<20]` returns all CT components whose load property is less than 20.

Path-based constructors can be used, for example, in rules:

```
invariant forall p in
    /self/components:CT[load<20]/ports:PT[i>0.5 and i < 1] | 
       attached (p);
```

is equivalent to:

```
invariant forall c : CT in self.components |
   c.load < 20 -> (forall p : PT in c.ports |
     (p.i > 0.5 and p.i < 1 -> attached (p)))
```
Or
```
invariant size (/self/components/ports:PT) > 0;
```
is equivalent to
```
invariant size ({select p in {collect c.ports in self.components | true} | satisfiesType (p, PT)}) > 0;
```
