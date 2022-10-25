# Copyright 2015 gRPC authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from concurrent import futures
import logging

import grpc
import python_pb2
import python_pb2_grpc

class Server(python_pb2_grpc.PythonServiceServicer):

    def Request(self, request, context):
        noOfMessagesProcessed = 0
        listOfWork = request.getListOfWorkList()
        for work in listOfWork:
                print("Server processed message with id - " + work.getId() +
                          " and type - " + work.getPayLoadType() + " and payload - " + work.getPayLoad())
                noOfMessagesProcessed += 1
        return python_pb2.Reply(message='response_after_work_processed')

def serve():
    port = '4444'
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=1))
    python_pb2_grpc.add_PythonServiceServicer_to_server(Server(), server)
    server.add_insecure_port('[::]:' + port)
    server.start()
    print("Server started, listening on " + port)
    server.wait_for_termination()


if __name__ == '__main__':
    logging.basicConfig()
    serve()