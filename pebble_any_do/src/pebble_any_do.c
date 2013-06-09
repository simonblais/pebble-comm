#include "pebble_os.h"
#include "pebble_app.h"
#include "pebble_fonts.h"

// 60113006-3F95-4AA8-8D82-0F28AB34C3E7
#define MY_UUID { 0x60, 0x11, 0x30, 0x06, 0x3F, 0x95, 0x4A, 0xA8, 0x8D, 0x82, 0x0F, 0x28, 0xAB, 0x34, 0xC3, 0xE7 }
PBL_APP_INFO(MY_UUID,
             "Pebble Any Do", "Simon Blais",
             1, 0, /* App version */
             DEFAULT_MENU_ICON,
             APP_INFO_STANDARD_APP);

////////////////////////////////////////////
// Data
////////////////////////////////////////////
Window window;
TextLayer textLayer;

enum Keys {
    REQ_KEY = 0x0,  // TUPPLE_INTEGER
    NOTIF_KEY = 0x1,// TUPPLE_CSTRING
};

enum RequestValues {
    LIST_REQ = 0x0,
};

////////////////////////////////////////////
// Functions
////////////////////////////////////////////

/// Ask the phone app to send current list
static void send_list_request() {
    Tuplet value = TupletInteger(REQ_KEY, LIST_REQ);

    DictionaryIterator *iter;
    app_message_out_get(&iter);

    if (!iter)
        return;

    dict_write_tuplet(iter, &value);
    dict_write_end(iter);

    app_message_out_send();
    app_message_out_release();

    static char textSending[] = "Sending Req";

    text_layer_set_text(&textLayer, textSending);
}

static void receive_new_list(char* newList) {
    static char list[256] = "New Msg";

    strcpy(list, newList);

    text_layer_set_text(&textLayer, list);
}

////////////////////////////////////////////
// Message Handling callbacks
////////////////////////////////////////////
void my_out_sent_handler(DictionaryIterator *sent, void *context) {
    // Out message delivered
    static char textSuccess[] = "Msg out delivered";

    text_layer_set_text(&textLayer, textSuccess);
}

void my_out_fail_handler(DictionaryIterator *failed, AppMessageResult reason,
                         void *context) {
    // Out message failed
    static char textFailed[] = "Msg out failed";

    text_layer_set_text(&textLayer, textFailed);
}

void my_in_rcv_handler(DictionaryIterator *received, void *context) {
    // incoming message received

    Tuple* inTuple = dict_find(received, NOTIF_KEY);

    if (inTuple == NULL) {
        // No valid info found
        static char textInvalid[] = "Invalid msg";

        text_layer_set_text(&textLayer, textInvalid);

        return;
    }

    receive_new_list(inTuple->value->cstring);
}

void my_in_drp_handler(void *context, AppMessageResult reason) {
    // incoming message dropped
}

////////////////////////////////////////////
// Initialization callbacks
////////////////////////////////////////////
void handle_init(AppContextRef ctx) {
  (void)ctx;

  window_init(&window, "Window Name");
  window_stack_push(&window, true /* Animated */);

  text_layer_init(&textLayer, GRect(2, 45, 144-4, 168-54));
  text_layer_set_font(&textLayer, fonts_get_system_font(FONT_KEY_GOTHIC_28_BOLD));

  layer_add_child(&window.layer, &textLayer.layer);

  send_list_request();
}

static void app_deinit(AppContextRef c) {
    (void) c;
}

////////////////////////////////////////////
// Main function
////////////////////////////////////////////
void pbl_main(void *params) {
  PebbleAppHandlers handlers = {
    .init_handler = &handle_init,
    .deinit_handler = &app_deinit,
    .messaging_info = {
          .default_callbacks.callbacks = {
              .out_sent = my_out_sent_handler,
              .out_failed = my_out_fail_handler,
              .in_received = my_in_rcv_handler,
              .in_dropped = my_in_drp_handler,
          },
          .buffer_sizes = {
              .inbound = 256,
              .outbound = 64,
          },
      },
  };
  app_event_loop(params, &handlers);
}
