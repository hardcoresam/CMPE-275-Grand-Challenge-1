# Generated by the gRPC Python protocol compiler plugin. DO NOT EDIT!
"""Client and server classes corresponding to protobuf-defined services."""
import grpc

import python_pb2 as python__pb2


class PythonServiceStub(object):
    """a service interface (contract)

    """

    def __init__(self, channel):
        """Constructor.

        Args:
            channel: A grpc.Channel.
        """
        self.request = channel.unary_unary(
                '/route.PythonService/request',
                request_serializer=python__pb2.Route.SerializeToString,
                response_deserializer=python__pb2.Route.FromString,
                )
        self.RecordRoute = channel.stream_unary(
                '/route.PythonService/RecordRoute',
                request_serializer=python__pb2.Route.SerializeToString,
                response_deserializer=python__pb2.RouteSummary.FromString,
                )


class PythonServiceServicer(object):
    """a service interface (contract)

    """

    def request(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def RecordRoute(self, request_iterator, context):
        """Accepts a stream of Routes on a route being traversed, returning a
        RouteSummary when traversal is completed.
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')


def add_PythonServiceServicer_to_server(servicer, server):
    rpc_method_handlers = {
            'request': grpc.unary_unary_rpc_method_handler(
                    servicer.request,
                    request_deserializer=python__pb2.Route.FromString,
                    response_serializer=python__pb2.Route.SerializeToString,
            ),
            'RecordRoute': grpc.stream_unary_rpc_method_handler(
                    servicer.RecordRoute,
                    request_deserializer=python__pb2.Route.FromString,
                    response_serializer=python__pb2.RouteSummary.SerializeToString,
            ),
    }
    generic_handler = grpc.method_handlers_generic_handler(
            'route.PythonService', rpc_method_handlers)
    server.add_generic_rpc_handlers((generic_handler,))


 # This class is part of an EXPERIMENTAL API.
class PythonService(object):
    """a service interface (contract)

    """

    @staticmethod
    def request(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/route.PythonService/request',
            python__pb2.Route.SerializeToString,
            python__pb2.Route.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def RecordRoute(request_iterator,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.stream_unary(request_iterator, target, '/route.PythonService/RecordRoute',
            python__pb2.Route.SerializeToString,
            python__pb2.RouteSummary.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)
