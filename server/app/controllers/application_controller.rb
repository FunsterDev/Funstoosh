class ApplicationController < ActionController::API
  include ActionController::Cookies
  include Concerns::SessionManagement
  include Concerns::ErrorHandling
end
