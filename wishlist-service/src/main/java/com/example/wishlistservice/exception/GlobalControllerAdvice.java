package com.example.wishlistservice.exception;

//@Slf4j
//@RestControllerAdvice
//public class GlobalControllerAdvice {
//
//    @ExceptionHandler(VitaQueueException.class)
//    public ResponseEntity<?> applicationHandler(VitaQueueException e) {
//      log.error("Error occurs {}", e.toString());
//        return ResponseEntity.status(e.getErrorCode().getStatus())
//                .body(ApiResponse.error(e.getErrorCode().name()));
//    }
//
//    @ExceptionHandler(RuntimeException.class)
//    public ResponseEntity<?> applicationHandler(RuntimeException e) {
//      log.error("Error occurs {}", e.toString());
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.name()));
//    }
//}
