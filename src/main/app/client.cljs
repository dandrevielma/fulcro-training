(ns app.client
  (:require
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom]
   [com.fulcrologic.fulcro.mutations :refer [defmutation]]
   [com.fulcrologic.fulcro.algorithms.tempid :as tempid]))

(defonce app (app/fulcro-app))

(defn new-book []
  {:id (tempid/tempid) :title "" :description ""})

(defmutation update-current-book [{:keys [field value]}]
  (action [{:keys [state]}]
          (swap! state assoc-in [:current-book field] value)))

(defmutation add-new-book [_]
  (action [{:keys [state]}]
          (let [current-book (:current-book @state)]
            (swap! state update :books conj current-book)
            (swap! state assoc :current-book (new-book)))))

(defmutation remove-book [{:keys [id]}]
  (action [{:keys [state]}]
          (swap! state update :books (fn [books] (remove #(= (:id %) id) books)))))

(defsc Root [this {:keys [books current-book]}]
  {:query [:books :current-book]
   :initial-state {:books [{:id 1 :title "Harry Potter - The Philosopher's Stone"
                            :description "Harry Potter and the Philosopher's Stone is a fantasy novel written by British author J. K. Rowling."}
                           {:id 2 :title "Pirates of the Caribbean"
                            :description "Pirates of the Caribbean is an American fantasy supernatural swashbuckler film series produced by Jerry Bruckheimer"}]
                   :current-book (new-book)}}
  (let [{:keys [title description]} current-book]
    (dom/div {:className "flex gap-[20px] items-center justify-center w-full h-full" :id "app-root"}
             (dom/div {:className "flex gap-[20px]"}
                      (dom/div
                       {:className "w-[460px] h-full py-[60px] flex flex-col gap-[20px] text-[2em] rounded-[20px] bg-white items-center justify-center shadow-[0_0_20px_rgba(0,0,0,0.1)]"
                        :id "app-content"}
                       (dom/input {:className "w-[80%] h-[40px] border-2 border-blue-200 rounded-[10px] py-[10px] text-[0.5em] pl-[25px]"
                                   :type "text"
                                   :placeholder "Book title"
                                   :value title
                                   :onChange #(comp/transact! this [(update-current-book {:field :title :value (-> % .-target .-value)})])})
                       (dom/input {:className "w-[80%] h-[40px] border-2 border-blue-200 rounded-[10px] py-[10px] text-[0.5em] pl-[25px]"
                                   :type "text"
                                   :placeholder "Book Description"
                                   :value description
                                   :onChange #(comp/transact! this [(update-current-book {:field :description :value (-> % .-target .-value)})])})
                       (dom/div {:type "text"
                                 :className "flex items-center justify-center bg-[#bed5fa] text-[#1c2026] font-[600] w-[80%] h-[40px] rounded-[10px] text-[0.5em] cursor-pointer hover:bg-[#a3c1f2] transition duration-150"
                                 :onClick #(comp/transact! this [(add-new-book {})])} "Add Book"))
                      (dom/div {:className "flex"}
                               (dom/div
                                {:className "w-[460px] px-[10px] h-[80vh] flex flex-col rounded-[20px] bg-white items-start justify-start pt-[10px] shadow-[0_0_20px_rgba(0,0,0,0.1)] gap-[10px]"
                                 :id "app-content"}
                                (for [book books]
                                  (let [{:keys [id title description]} book]
                                    (dom/div {:key id
                                              :className "bg-[#bed5fa] hover:bg-[#ff8585] cursor-pointer p-6 flex flex-col gap-[10px] rounded-[10px] transition duration-150 w-full"
                                              :onClick #(comp/transact! this [(remove-book {:id id})])}
                                             (dom/div {:className " flex justify-start items-center gap-[10px]"}
                                                      (dom/img {:src "https://cdn-icons-png.flaticon.com/512/3864/3864835.png" :className "w-[24px] h-[24px]"})
                                                      (dom/h1 {:className "text-[16px] font-bold"} title))
                                             (dom/p {:className "text-[12px] font-regular text-[#606060]"} description))))))))))

(defn ^:export init []
  (app/mount! app Root "app")
  (js/console.log "Loaded"))

(defn ^:export refresh []
  (app/mount! app Root "app")
  (js/console.log "Hot reload"))
