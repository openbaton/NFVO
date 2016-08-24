tosca.nodes.nfv.CP:
  derived_from: tosca.nodes.Root
  properties:
    type:
      type: string
      required: false
  requirements:
    - virtualLink:
        capability: tosca.capabilities.VirtualLinkable
    - virtualbinding:
        capability: tosca.capabilities.nfv.Virtualbindable
  attributes:
    IP_address:
      type: string
      required: false