Rails.application.routes.draw do
  resources :users, only: [:create, :index]
  resources :games, only: [:create]
  resources :pictures, only: [:create, :show]
end
