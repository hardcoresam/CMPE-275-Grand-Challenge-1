// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: userMessage.proto

package services;

public interface MessageOrBuilder extends
    // @@protoc_insertion_point(interface_extends:services.Message)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int64 id = 1;</code>
   * @return The id.
   */
  long getId();

  /**
   * <code>int64 origin = 2;</code>
   * @return The origin.
   */
  long getOrigin();

  /**
   * <code>int64 destination = 3;</code>
   * @return The destination.
   */
  long getDestination();

  /**
   * <code>bytes payload = 5;</code>
   * @return The payload.
   */
  com.google.protobuf.ByteString getPayload();
}
